package com.babeeta.butterfly.application.gateway.ios.impl;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.gateway.ios.AccountService;

public class PooledApnConnectionFactory implements KeyedPoolableObjectFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(PooledApnConnectionFactory.class);

	private final AccountService accountService;

	public static final String APNS_PRODUCTION = "gateway.push.apple.com";
	public static final String APNS_SANDBOX = "gateway.sandbox.push.apple.com";

	// 连接的最长生存时间
	private static final long MAX_AGE = 30 * 1000;

	public PooledApnConnectionFactory(AccountService certService) {
		super();
		this.accountService = certService;
	}

	@Override
	public void activateObject(Object key, Object obj) throws Exception {

	}

	@Override
	public void destroyObject(Object key, Object obj) throws Exception {
		logger.debug("Destroing connection for {}",
				((ApnsConnection) obj).getApplicationId());
		ApnsConnection conn = (ApnsConnection) obj;
		conn.close();
	}

	public AccountService getCertService() {
		return accountService;
	}

	@Override
	public Object makeObject(Object key) throws Exception {
		logger.debug("Making new connection for {}", key);
		SSLSocket socket = createNewSocket(key.toString());
		return new ApnsConnectionImpl(key.toString(), socket,
				accountService.isApplicationInDevelopment(key.toString()));
	}

	@Override
	public void passivateObject(Object key, Object obj) throws Exception {

	}

	@Override
	public boolean validateObject(Object key, Object obj) {
		logger.debug("[{}]Validating connection for {}", obj.toString(), key);
		ApnsConnection conn = (ApnsConnection) obj;
		return (System.currentTimeMillis() - conn.getCreationTime()) <= MAX_AGE
				&& conn.validate();
	}

	private SSLSocket createNewSocket(String applicationId) throws Exception {
		KeyStore keystore = accountService
				.findByApplicationId(applicationId);
		if (keystore == null) {
			logger.error("[{}]Cannot find keystore.", applicationId);
			return null;
		} else {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("sunX509");
			kmf.init(keystore, DigestUtils.md5Hex(applicationId).toCharArray());
			sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
			SSLSocket socket = (SSLSocket) sslContext
					.getSocketFactory()
					.createSocket(
							accountService.isApplicationInDevelopment(applicationId) ? APNS_SANDBOX
									: APNS_PRODUCTION, 2195);
			socket.startHandshake();
			return socket;
		}
	}
}