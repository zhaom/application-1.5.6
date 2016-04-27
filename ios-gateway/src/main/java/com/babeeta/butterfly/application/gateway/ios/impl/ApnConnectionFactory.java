package com.babeeta.butterfly.application.gateway.ios.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApnConnectionFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(ApnConnectionFactory.class);

	private final ApnsConnectionPool apnsConnectionPool;

	public ApnConnectionFactory(ApnsConnectionPool apnsConnectionPool) {
		super();
		this.apnsConnectionPool = apnsConnectionPool;
	}

	public ApnsConnection getConnection(String applicationId) throws Exception {
		logger.debug("Requesting connection for {}", applicationId);
		return (ApnsConnection) apnsConnectionPool.borrowObject(applicationId);
	}

}
