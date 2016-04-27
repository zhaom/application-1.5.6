package com.babeeta.butterfly.application.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.app.status.MessageStatusListener;
import com.babeeta.butterfly.router.network.Service;

public class AppService {
	private static final Logger logger = LoggerFactory
			.getLogger(AppService.class);

	public static void start() {
		logger.info("app-service status listener service startup begin.");
		try {
			bootstrap();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("java ....AppService startup failed!!!");
		}
		logger.info("app-service status listener service startup successful.");
	}

	private static void bootstrap() throws Exception {
		final ExecutorService executorService = Executors.newCachedThreadPool();
		MessageStatusListener messageStatusListener = new MessageStatusListener();
		final Service service = new Service(messageStatusListener,
				executorService,
				executorService);
		service.start("app");
		Runtime.getRuntime().removeShutdownHook(new Thread("Shutdown") {
			@Override
			public void run() {
				service.shutdownGraceFully();
				executorService.shutdownNow();
			}
		});
	}
}
