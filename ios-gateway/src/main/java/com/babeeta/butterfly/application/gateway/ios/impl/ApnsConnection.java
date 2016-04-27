package com.babeeta.butterfly.application.gateway.ios.impl;

import java.io.IOException;

import com.babeeta.butterfly.application.gateway.ios.ApnsNotification;

public interface ApnsConnection {

	public abstract void close();

	public abstract String getApplicationId();

	/**
	 * 查询创建时间
	 * 
	 * @return
	 */
	public long getCreationTime();

	public abstract boolean isDevelopment();

	/**
	 * 发送消息
	 * 
	 * @param notification
	 * @throws IOException
	 */
	public abstract void send(ApnsNotification notification) throws IOException;

	public abstract boolean validate();

}