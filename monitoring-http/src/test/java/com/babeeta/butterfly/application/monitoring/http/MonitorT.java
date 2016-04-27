package com.babeeta.butterfly.application.monitoring.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.babeeta.butterfly.application.monitoring.http.monitor.AccountMonitor;
import com.babeeta.butterfly.application.monitoring.http.monitor.ThirdAppMonitor;

public class MonitorT {
	ThirdAppMonitor thirdMonitor = new ThirdAppMonitor();

	AccountMonitor devAccountMonitor = new AccountMonitor("211.147.215.36");
	AccountMonitor appAccountMonitor = new AccountMonitor("211.147.215.36");

	@Test
	public void testAppAccountMonitor() {
		try {
			HttpResponse response = appAccountMonitor.request();
			TestCase.assertEquals(response.getStatusLine().getStatusCode(), 200);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testDevAccountMonitor() {
		try {
			HttpResponse response = devAccountMonitor.request();
			TestCase.assertEquals(response.getStatusLine().getStatusCode(), 200);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testThirdMonitor() {
		try {
			String url = "http://push-api-0.hudee.com/service/client/44bbcd579c294406b0099df1362b472d/message";
			HttpResponse response = thirdMonitor.request(url);
			TestCase.assertEquals(response.getStatusLine().getStatusCode(), 200);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
