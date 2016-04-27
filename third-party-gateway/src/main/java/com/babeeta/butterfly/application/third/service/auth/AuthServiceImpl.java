package com.babeeta.butterfly.application.third.service.auth;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

public class AuthServiceImpl implements AuthService {

	private HttpClient client;
	private HttpPost httpPost;

	public AuthServiceImpl() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		// Create and initialize scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));

		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(
				schemeRegistry);
		threadSafeClientConnManager.setMaxTotal(100);
		threadSafeClientConnManager.setDefaultMaxPerRoute(50);

		client = new DefaultHttpClient(threadSafeClientConnManager, params);
		httpPost = new HttpPost("http://accounts.app/api/auth");
		httpPost.setHeader(new BasicHeader("Content-type", "application/json"));
	}

	@Override
	public AuthResult authenticate(String appId, String appKey) {
		// do authorization
		String json = "{\"id\":\"" + appId + "\",\"secureKey\":\"" + appKey
				+ "\"}";
		httpPost.setEntity(new ByteArrayEntity(json.getBytes()));

		HttpResponse httpResponse = null;
		try {
			httpResponse = client.execute(httpPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (HttpStatus.SC_OK != httpResponse.getStatusLine()
				.getStatusCode()) {
			return new AuthResult(false, AuthFailedReason.serverInternalError);
		}
		JSONObject obj = null;
		try {
			obj = JSONObject.fromObject(EntityUtils
					.toString(httpResponse.getEntity()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if ("OK".equalsIgnoreCase(obj.get("status").toString())) {
			return new AuthResult(true, null);
		} else if ("FREEZED".equals(obj.get("status").toString())) {
			return new AuthResult(false, AuthFailedReason.freezed);
		} else {
			return new AuthResult(false, AuthFailedReason.unmatched);
		}
	}
}
