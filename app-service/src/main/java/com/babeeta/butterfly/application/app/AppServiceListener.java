package com.babeeta.butterfly.application.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppServiceListener implements ServletContextListener {
	private static final Logger logger = LoggerFactory
			.getLogger(AppServiceListener.class);

	@Override
	public void contextDestroyed(ServletContextEvent event) {

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		logger.debug("App service listener starting");
		AppService.start();
		logger.debug("App service listener started.");
	}

}
