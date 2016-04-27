package com.babeeta.butterfly.application.gateway.ios.impl;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.gateway.ios.ApnsNotification;

public class ApnsConnectionImpl implements ApnsConnection {

	private static final Logger logger = LoggerFactory
			.getLogger(ApnsConnectionImpl.class);

	private final String applicationId;
	private final Socket socket;
	private final boolean development;
	private final long creationTime;

	public ApnsConnectionImpl(String applicationId, Socket socket,
			boolean development) {
		super();
		this.applicationId = applicationId;
		this.socket = socket;
		this.development = development;
		this.creationTime = System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.babeeta.butterfly.application.gateway.ios.impl.ApnsConnection#close()
	 */
	@Override
	public void close() {
		if (socket != null && socket.isConnected()) {
			try {
				socket.close();
			} catch (IOException e) {
				logger.error("[{}]Errror ocurred while closing the socket. {}",
						applicationId, e.getMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.babeeta.butterfly.application.gateway.ios.impl.ApnsConnection#
	 * getApplicationId()
	 */
	@Override
	public String getApplicationId() {
		return applicationId;
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.babeeta.butterfly.application.gateway.ios.impl.ApnsConnection#
	 * isDevelopment()
	 */
	@Override
	public boolean isDevelopment() {
		return development;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.babeeta.butterfly.application.gateway.ios.impl.ApnsConnection#send
	 * (com.babeeta.butterfly.application.gateway.ios.ApnsNotification)
	 */
	@Override
	public void send(ApnsNotification notification) throws IOException {
		try {
			socket.getOutputStream().write(notification.marshall());
			socket.getOutputStream().flush();
			logger.info("[{}]has been sent to apns.", notification.getUid());
		} catch (Exception ex) {
			logger.error(ex.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.babeeta.butterfly.application.gateway.ios.impl.ApnsConnection#validate
	 * ()
	 */
	@Override
	public boolean validate() {
		try {
			socket.setSoTimeout(10);
			try {
				socket.getInputStream().read();
			} catch (SocketTimeoutException ignore) {
			}
			return true;
		} catch (Exception e) {
			logger.error("[{}]Connection of {} is broken. {}", new Object[] {
					socket == null ? "Unkown" : socket.getLocalPort(),
					applicationId,
					e.getMessage() });
		}
		return false;
	}
}