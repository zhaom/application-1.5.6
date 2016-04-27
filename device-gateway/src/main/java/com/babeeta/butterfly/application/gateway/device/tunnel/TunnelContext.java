package com.babeeta.butterfly.application.gateway.device.tunnel;

import org.jboss.netty.channel.Channel;

import com.shangmail.tunnel.timoutpolicy.TimeoutPolicy;

public class TunnelContext {
	private TimeoutPolicy currentTimeoutPolicy = null;
	private Channel channel = null;
	private String deviceId;

	public Channel getChannel() {
		return channel;
	}

	public TimeoutPolicy getCurrentTimeoutPolicy() {
		return currentTimeoutPolicy;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setCurrentTimeoutPolicy(TimeoutPolicy currentTimeoutPolicy) {
		this.currentTimeoutPolicy = currentTimeoutPolicy;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	void setChannel(Channel channel) {
		this.channel = channel;
	}

}
