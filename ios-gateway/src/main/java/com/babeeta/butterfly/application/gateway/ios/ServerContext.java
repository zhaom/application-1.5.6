package com.babeeta.butterfly.application.gateway.ios;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerContext {
	public final String domain = "ios.dev";
	public final ExecutorService bossThreadPool = Executors
			.newCachedThreadPool();
	public final ExecutorService workerThreadPool = Executors
			.newCachedThreadPool();

}