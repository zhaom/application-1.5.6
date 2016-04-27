package com.babeeta.butterfly.application.monitoring.http.monitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdAppMonitor implements HttpMonitor {

	private final static Logger logger = LoggerFactory
			.getLogger(ThirdAppMonitor.class);
	HttpClient httpClient = new DefaultHttpClient();

	@Override
	public void executeHttp() {
		try {
			HttpResponse response = request("http://push-api-0.hudee.com/service/client/44bbcd579c294406b0099df1362b472d/message");

			if (response.getStatusLine().getStatusCode() == 200) {
				logger.info(
						"Thirdapp response success, Status code:{},reponse entry:{}",
						response.getStatusLine(),
						EntityUtils.toString(response.getEntity()));
			} else {
				logger.warn(
						"Thirdapp response failed, Status code:{},reponse entry:{}",
						response.getStatusLine(),
						EntityUtils.toString(response.getEntity()));
			}
		} catch (final IOException e) {
			logger.warn("Error on execute request:" + e.getMessage());
		}
	}

	public HttpResponse request(String url)
			throws UnsupportedEncodingException,
			IOException, ClientProtocolException {
		String appId = "44bbcd579c294406b0099df1362b472d";
		String appKey = "2350b2fed2b2409ba15acff2841c8b5c";
		String content = "Monitor request!";
		HttpPost request = new HttpPost(url);
		byte[] token = (appId + ":" + appKey).getBytes("utf-8");
		String authorization = "Basic "
				+ new String(Base64.encodeBase64(token), "utf-8");
		request.addHeader("Authorization", authorization);
		request.addHeader("exptime", "20");
		ByteArrayEntity byteArray = new ByteArrayEntity(
				content.getBytes("utf-8"));
		request.setEntity(byteArray);

		HttpResponse response = httpClient.execute(request);
		return response;
	}
}
