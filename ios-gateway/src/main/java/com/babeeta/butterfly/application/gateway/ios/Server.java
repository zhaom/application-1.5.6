package com.babeeta.butterfly.application.gateway.ios;

import com.babeeta.butterfly.application.gateway.ios.impl.AccountServiceImpl;
import com.babeeta.butterfly.application.gateway.ios.impl.ApnConnectionFactory;
import com.babeeta.butterfly.application.gateway.ios.impl.ApnServiceImpl;
import com.babeeta.butterfly.application.gateway.ios.impl.ApnsConnectionPool;
import com.babeeta.butterfly.router.network.Service;

public class Server {
	public static void main(String[] args) {
		try {
			ServerContext serverContext = new ServerContext();
			bootstrap(serverContext);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void bootstrap(ServerContext serverContext)
			throws Exception {

		// 这个是用来监听ios.dev的。走LVS的。
		Service service = new Service(new IosMessageHandler(
				new ApnServiceImpl(new ApnConnectionFactory(
						new ApnsConnectionPool(new AccountServiceImpl())))),
				serverContext.bossThreadPool, serverContext.workerThreadPool);

		service.start(serverContext.domain);
	}

}