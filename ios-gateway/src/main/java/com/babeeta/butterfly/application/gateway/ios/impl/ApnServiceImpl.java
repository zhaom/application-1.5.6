package com.babeeta.butterfly.application.gateway.ios.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.MessageRouting.Message;
import com.babeeta.butterfly.application.gateway.ios.ApnService;
import com.babeeta.butterfly.application.gateway.ios.ApnsNotification;

public class ApnServiceImpl implements ApnService {

	class AsyncSender implements Runnable {
		final Message message;
		final AtomicInteger counter = new AtomicInteger();
		static final int MAX_RETRY = 2;

		public AsyncSender(Message message) {
			super();
			this.message = message;
		}

		@Override
		public void run() {
			String applicationId = getApplicationId(message);
			assert applicationId.matches("a-fA-F0-9");
			String deviceToken = getDeviceToken(message);
			ApnsConnection apnsConnection = null;
			try {
				apnsConnection = apnConnectionFactory
						.getConnection(applicationId);
				ApnsNotification notification = new ApnsNotification(
						message.getUid(),
						deviceToken, message.getContent().toStringUtf8());
				apnsConnection.send(notification);
			} catch (Exception e) {
				if (counter.incrementAndGet() <= MAX_RETRY) {
					// 会有一次立即重试，应对连接可能僵死的情况。因为连接池的testOnBorrow为false
					logger.error("[{}]Sending failed. {}.  Retrying...",
							message.getUid(),
							e.getMessage());
					executorService.execute(this);
				} else {
					logger.error("[{}]Sending failed. {}.  Give up.",
							message.getUid(),
							e.getMessage());
				}
			} finally {
				if (apnsConnection != null) {
					apnsConnection.close();
				}
			}
		}
	}

	private static final Logger logger = LoggerFactory
			.getLogger(ApnServiceImpl.class);
	private final ApnConnectionFactory apnConnectionFactory;
	private final int expiry = 24 * 60 * 60;

	private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
			32,
			32, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

	public ApnServiceImpl(ApnConnectionFactory apnConnectionFactory) {
		super();
		this.apnConnectionFactory = apnConnectionFactory;
	}

	@Override
	public void send(Message message) {
		executorService.execute(new AsyncSender(message));
	}

	private String getApplicationId(Message message) {
		return message.getFrom().substring(0, message.getFrom().indexOf("@"));
	}

	private String getDeviceToken(Message message) {
		return message.getTo().substring(0, message.getTo().indexOf("@"));
	}
}
