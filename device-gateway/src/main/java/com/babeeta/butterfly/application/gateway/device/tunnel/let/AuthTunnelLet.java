package com.babeeta.butterfly.application.gateway.device.tunnel.let;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.MessageRouting;
import com.babeeta.butterfly.MessageRouting.Credential;
import com.babeeta.butterfly.MessageRouting.Message;
import com.babeeta.butterfly.MessageRouting.Response;
import com.babeeta.butterfly.application.gateway.device.ServerContext;
import com.babeeta.butterfly.application.gateway.device.push.MessagePusher;
import com.babeeta.butterfly.application.gateway.device.tunnel.TunnelContext;
import com.babeeta.butterfly.application.gateway.device.tunnel.TunnelData;
import com.babeeta.butterfly.application.gateway.device.tunnel.TunnelLet;
import com.babeeta.butterfly.application.gateway.device.tunnel.let.monitor.ThreadPool;
import com.babeeta.butterfly.application.reliable.ReliablePush;
import com.babeeta.butterfly.application.reliable.ReliablePushImpl;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

public class AuthTunnelLet implements TunnelLet<Credential>, AuthTunnelLetMBean {

	private static final Logger logger = LoggerFactory
			.getLogger(AuthTunnelLet.class);

	private final ServerContext serverContext;

	private final AtomicInteger requestCounter = new AtomicInteger();
	private final AtomicInteger successCounter = new AtomicInteger();
	private final AtomicInteger failedCounter = new AtomicInteger();
	private final AtomicInteger serviceUnavailableCounter = new AtomicInteger();

	private final int MAX_CONNECTION = 256;

	public final ThreadPoolExecutor authExecutor = new ThreadPoolExecutor(64,
			MAX_CONNECTION, 10,
			TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(1000));
	public final ThreadPoolExecutor reliablePushExecutor = new ThreadPoolExecutor(
			64, MAX_CONNECTION, 10,
			TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(1000));

	private HttpClient httpClient;

	private final UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			logger.error("Uncaught exception: ", e);

		}
	};

	private ThreadSafeClientConnManager httpClientConnectionManager;

	public AuthTunnelLet(ServerContext serverContext) {
		super();
		this.serverContext = serverContext;
		this.serverContext.registerMBean(
				"TunnelLet." + AuthTunnelLet.class.getSimpleName()
						+ ":name=AuthExecutor", new ThreadPool(authExecutor));
		this.serverContext.registerMBean(
				"TunnelLet." + AuthTunnelLet.class.getSimpleName()
						+ ":name=ReliablePushExecutor", new ThreadPool(
						reliablePushExecutor));
		this.serverContext.registerMBean("TunnelLet:name=AuthTunnelLet", this);
		httpClientOptions();
	}

	@Override
	public int getFailedCount() {
		return failedCounter.getAndSet(0);
	}

	@Override
	public int getHttpClientConnectionsInPool() {
		return httpClientConnectionManager.getConnectionsInPool();
	}

	@Override
	public int getRequestCount() {
		return requestCounter.getAndSet(0);
	}

	@Override
	public int getServiceUnavailableCount() {
		return serviceUnavailableCounter.getAndSet(0);
	}

	@Override
	public int getSuccessCount() {
		return successCounter.getAndSet(0);
	}

	@Override
	public void messageReceived(
			final TunnelContext tunnelContext, final TunnelData<Credential> data) {
		requestCounter.incrementAndGet();
		try {
			authExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Thread.currentThread().setName("Auth");
					Thread.currentThread().setUncaughtExceptionHandler(
							uncaughtExceptionHandler);
					onAuth(tunnelContext, data);
				}

			});
		} catch (RejectedExecutionException e) {
			serviceUnavailableCounter.incrementAndGet();
			logger.error("[{}]Auth failed due to over load.", tunnelContext
					.getChannel().getId());
			tunnelContext.getChannel().write(
					new TunnelData<MessageLite>(data.tag,
							MessageRouting.MessageCMD.RESPONSE.getNumber(),
							MessageRouting.Response
									.newBuilder()
									.setStatus("SERVICE_UNAVAILABLE")
									.build()));
		}
	}

	private String convertToJson(Credential credential) {
		return "{\"id\":\"" + credential.getId() + "\",\"secureKey\":\""
				+ credential.getSecureKey() + "\"}";
	}

	private void doOk(final TunnelContext tunnelContext,
			final TunnelData<Credential> data, Credential credential,
			HttpEntity entity) throws IOException {
		JSONObject obj = JSONObject.fromObject(EntityUtils
				.toString(entity));
		if ("OK".equalsIgnoreCase(obj.get("status").toString())) {
			successCounter.incrementAndGet();
			tunnelContext.setDeviceId(data.obj.getId());
			MessagePusher.getDefaultInstance().register(
					data.obj.getId(), tunnelContext.getChannel());
			reportToGateway(data.obj.getId());
			tunnelContext.getChannel()
					.write(
							new TunnelData<MessageLite>(data.tag, 135,
									Response
											.newBuilder()
											.setStatus("SUCCESS")
											.build()));
			logger.debug("[{}] Sent success to client. [{}]",
					tunnelContext.getChannel().getId(), obj.toString());
			reliablePush(tunnelContext, data);
		} else {
			failedCounter.incrementAndGet();
			logger.debug("[{}] Device Auth failed. [{}:{}]"
					, new Object[] { tunnelContext.getChannel().getId(),
							credential.getId(), credential.getSecureKey() });
			sendError(tunnelContext, data);
		}
	}

	private void httpClientOptions() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
					new Scheme("http", 80, PlainSocketFactory
							.getSocketFactory()));

		httpClientConnectionManager = new ThreadSafeClientConnManager(
				schemeRegistry);

		httpClientConnectionManager.setMaxTotal(MAX_CONNECTION);
		httpClientConnectionManager.setDefaultMaxPerRoute(MAX_CONNECTION);
		httpClient = new DefaultHttpClient(httpClientConnectionManager);
	}

	private void onAuth(final TunnelContext tunnelContext,
			final TunnelData<Credential> data) {
		logger.info("[{}] Auth from [{}]", data.obj.getId(),
				tunnelContext
						.getChannel().getRemoteAddress());
		long timestamp = System.currentTimeMillis();
		Credential credential = data.obj;
		String requestBody = convertToJson(credential);

		HttpPost httpPost = new HttpPost("http://accounts.dev/api/auth");
		httpPost.setHeader(new BasicHeader("Content-type",
				"application/json"));
		httpPost.setEntity(new ByteArrayEntity(requestBody.getBytes()));
		HttpEntity entity = null;
		try {
			HttpResponse httpResponse = httpClient.execute(
					httpPost);
			int status = httpResponse.getStatusLine().getStatusCode();
			logger.debug("[{}]Got auth response: {}, time:{}ms",
					new Object[] { tunnelContext.getChannel().getId(), status,
							(System.currentTimeMillis() - timestamp) });
			entity = httpResponse.getEntity();
			if (HttpStatus.SC_OK == status) {
				doOk(tunnelContext, data, credential, entity);
			} else {
				logger.error(
						"Unexpectant response status :{} while auth on {}.",
						status, "accounts.dev");
				sendError(tunnelContext, data);
			}
		} catch (IOException e) {
			logger.error("Error occured while request [{}],{}",
					"accounts.dev",
					e.getMessage());
			sendError(tunnelContext, data);
		} finally {
			if (entity != null) {
				try {
					EntityUtils.consume(entity);
				} catch (IOException ignore) {
				}
			}
		}
	}

	private void onReliablePush(final TunnelContext tunnelContext,
			final TunnelData<Credential> data) {
		Thread.currentThread().setName("Reliable-Push");
		ReliablePush reliablePush = ReliablePushImpl
				.getDefaultInstance();
		List<MessageRouting.Message> list = reliablePush
				.getMessagesList(data.obj.getId());
		logger.debug("[{}] ReliablePush sending [{}]",
				data.obj.getId(), list.size());
		for (MessageRouting.Message message : list) {
			tunnelContext.getChannel().write(
					new TunnelData<MessageLite>(0, 129, message));
		}
	}

	private void reliablePush(final TunnelContext tunnelContext,
			final TunnelData<Credential> data) {
		try {
			reliablePushExecutor.execute(new Runnable() {
				@Override
				public void run() {
					onReliablePush(tunnelContext, data);
				}

			});
		} catch (RejectedExecutionException e) {
			logger.error("[{}][{}]Reliable push failed due to overload.",
					tunnelContext.getChannel().getId(),
					tunnelContext.getDeviceId());
		}
	}

	private void reportToGateway(String id) {
		logger.debug("[{}]Report to gateway.dev.", id);
		Message msg = Message
				.newBuilder()
				.setContent(ByteString.copyFromUtf8(id))
				.setDate(System.currentTimeMillis())
				.setFrom(
						"report@" + serverContext.messageServiceAddress)
				.setTo("update@gateway.dev")
				.setUid(UUID.randomUUID().toString()
						.replaceAll("\\-", ""))
				.build();
		serverContext.messageSender.send(msg);
	}

	private void sendError(final TunnelContext tunnelContext,
			final TunnelData<Credential> data) {
		tunnelContext.getChannel().write(
				new TunnelData<MessageLite>(data.tag, 135, Response
						.newBuilder().setStatus("ERROR").build()));
	}
}
