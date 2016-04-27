package com.babeeta.butterfly.application.gateway.ios.impl;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.babeeta.butterfly.application.gateway.ios.ApnsNotification;

@Ignore
public class ApnsNotificationTest {
	private static final Pattern pattern = Pattern.compile("[ -]");

	public static byte[] decodeHex(String deviceToken) {
		String hex = pattern.matcher(deviceToken).replaceAll("");

		byte[] bts = new byte[hex.length() / 2];
		for (int i = 0; i < bts.length; i++) {
			bts[i] = (byte) (charval(hex.charAt(2 * i)) * 16 + charval(hex
					.charAt(2 * i + 1)));
		}
		return bts;
	}

	private static int charval(char a) {
		if ('0' <= a && a <= '9') {
			return (a - '0');
		} else if ('a' <= a && a <= 'f') {
			return (a - 'a') + 10;
		} else if ('A' <= a && a <= 'F') {
			return (a - 'A') + 10;
		} else {
			throw new RuntimeException("Invalid hex character: " + a);
		}
	}

	private ApnsNotification notification;

	@Before
	public void setUp() throws Exception {
		notification = new ApnsNotification(
				"UUID",
				"713760f5501a6bf3270b9d8ab35c9608bf482b360cdce85c73a393ff5988f327",
				"{aps:{\"alert\":\"Hello from Liang's unit test.\", \"sound\":\"default\"}}");

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMarshall() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bout);

		out.writeByte(0);
		// out.writeInt(10);
		// out.writeInt(1000);
		byte[] token = decodeHex("713760f5501a6bf3270b9d8ab35c9608bf482b360cdce85c73a393ff5988f327");
		out.writeShort(token.length);
		out.write(token);
		String payload = "{aps:{\"alert\":\"Hello from Liang's unit test.\", \"sound\":\"default\"}}";
		out.writeShort(payload.getBytes().length);
		out.write(payload.getBytes());
		out.close();

		assertArrayEquals(bout.toByteArray(), notification.marshall());
	}
}