package com.babeeta.butterfly.application.monitoring.http.monitor;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountMonitor implements HttpMonitor {

	private final static Logger logger = LoggerFactory
			.getLogger(AccountMonitor.class);
	String target;

	HttpClient httpClient = new DefaultHttpClient();

	public AccountMonitor(String target) {
		this.target = target;
	}

	@Override
	public void executeHttp() {
		try {
			HttpResponse response = request();
			if (response.getStatusLine().getStatusCode() == 200) {
				logger.info(
						target
								+ " response success, Status code:{},reponse entry:{}",
						response.getStatusLine(),
						EntityUtils.toString(response.getEntity()));
			} else {
				logger.warn(
						target
								+ " response failed, Status code:{},reponse entry:{}",
						response.getStatusLine(),
						EntityUtils.toString(response.getEntity()));
			}
		} catch (final IOException e) {
			logger.error("Error on execute request:" + e.getMessage());
		}
	}

	public HttpResponse request() throws IOException, ClientProtocolException {
		String appId = "bcc6cffd44ba4dc5be699d13dc6da1f1";
		String appKey = "4df1b0a0e8e9427d977d18e7d6eadd8a";
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(
				"http://" + target + "/api/auth");
		// list.add(new BasicNameValuePair("appKey", appKey));
		request.setHeader(new BasicHeader("Content-type", "application/json"));
		String json = "{\"id\":\""
				+ appId
				+ "\",\"secureKey\":\""
				+ appKey
				+ "\"}";
		ByteArrayEntity byteArray = new ByteArrayEntity(json.getBytes());
		request.setEntity(byteArray);

		HttpResponse response = client.execute(request);
		return response;
	}
}
