package com.babeeta.butterfly.application.gateway.ios;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

import com.babeeta.butterfly.MessageRouting.Message;
import com.babeeta.butterfly.MessageRouting.MessageType;
import com.google.protobuf.ByteString;

public class Client {

	/**
	 * @param args
	 * @throws Exception
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) throws Exception {
		Socket s = new Socket("127.0.0.2", 5757);
		Message message = Message
				.newBuilder()
				.setContent(
						ByteString
								.copyFromUtf8("{\"aps\":{\"alert\":\"Hello, this is liang from hudee.\",\"badge\":22, \"sound\":\"default\"}}"))
				.setDate(System.currentTimeMillis())
				.setFrom("48857174287d4dbcaf87bfc17c8bd198@app")
				.setMessageType(MessageType.NOTIFICATION)
				.setTo("713760F5501A6BF3270B9D8AB35C9608BF482B360CDCE85C73A393FF5988F327@ios.dev")
				.setUid(UUID.randomUUID().toString())
				.build();

		message.writeDelimitedTo(s.getOutputStream());
		s.getOutputStream().flush();
		s.close();

	}

}
