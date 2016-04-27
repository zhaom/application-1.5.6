import java.net.InetSocketAddress;
import java.net.Socket;

import com.babeeta.butterfly.MessageRouting.Message;
import com.google.protobuf.ByteString;

public class Client {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Message msg = Message.newBuilder()
				.setFrom("rpc@0.gateway.dev")
				.setTo("bind@subscription.dev")
				.setDate(System.currentTimeMillis())
				.setContent(ByteString.copyFromUtf8("apid:cid"))
				.setUid("UUID" + System.nanoTime())
				.build();

		Socket socket = new Socket();
		socket.connect(new InetSocketAddress("127.0.0.6", 5757));

		msg.writeDelimitedTo(socket.getOutputStream());

		socket.close();

	}

}
