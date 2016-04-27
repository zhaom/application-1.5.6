package com.babeeta.butterfly.application.gateway.ios;

import java.net.Socket;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.babeeta.butterfly.MessageRouting.Message;
import com.google.protobuf.ByteString;

/**
 * Server类的单元测试。要求本地有ios.dev和0.ios.dev的域名
 * 
 * @author leon
 * 
 */
@Ignore
public class ServerTest {
	@Before
	public void setup() {
		Server.main(new String[] { "-n", "0" });
	}

	@Test
	public void test() throws Exception {
		Message message = Message
				.newBuilder()
				.setUid("UID")
				.setContent(
						ByteString
								.copyFromUtf8("{aps:{alert:'hi', badge:21}, text:'How are you.'}"))
				.setDate(System.currentTimeMillis())
				.setExpire(0)
				.setFrom("aid@app")
				.setTo("aabbccddeff@ios.dev")
				.build();

		Socket s = new Socket("ios.dev", 5757);
		message.writeDelimitedTo(s.getOutputStream());
		s.getOutputStream().flush();
	}
}
