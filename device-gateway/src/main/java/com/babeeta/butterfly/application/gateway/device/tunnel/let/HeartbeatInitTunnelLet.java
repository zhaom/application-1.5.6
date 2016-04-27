package com.babeeta.butterfly.application.gateway.device.tunnel.let;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.MessageRouting.HeartbeatInit;
import com.babeeta.butterfly.MessageRouting.HeartbeatInit.HeartbeatException;
import com.babeeta.butterfly.MessageRouting.HeartbeatResponse;
import com.babeeta.butterfly.application.gateway.device.tunnel.TunnelContext;
import com.babeeta.butterfly.application.gateway.device.tunnel.TunnelData;
import com.babeeta.butterfly.application.gateway.device.tunnel.TunnelLet;
import com.google.protobuf.MessageLite;
import com.shangmail.tunnel.timoutpolicy.ConfigurationProvider;
import com.shangmail.tunnel.timoutpolicy.TimeoutPolicyFactory;

public class HeartbeatInitTunnelLet implements TunnelLet<HeartbeatInit> {

	private final static Logger logger = LoggerFactory
			.getLogger(HeartbeatInitTunnelLet.class);

	private final TimeoutPolicyFactory timeoutPolicyFactory;

    public static AtomicLong HEARTBEAT_COUNT_INIT = new AtomicLong(0);

	public HeartbeatInitTunnelLet() {
		timeoutPolicyFactory = new TimeoutPolicyFactory();
		timeoutPolicyFactory
				.setConfigurationProvider(new ConfigurationProvider() {
					@Override
					public int getDecrement() {
						return 40;
					}

					@Override
					public int getDefault() {
						return 50;
					}

					@Override
					public int getIncrement() {
						return 30;
					}

					@Override
					public int getMax() {
						return 300;
					}

					@Override
					public int getMin() {
						return 50;
					}

					@Override
					public int getPreserveThreshold() {
						return 40;
					}
				});
	}

	@Override
	public void messageReceived(
			TunnelContext tunnelContext,
			TunnelData<HeartbeatInit> data) {
        HEARTBEAT_COUNT_INIT.getAndIncrement();
		logger.debug("[{}]e:{}, delay:{}, cause:{}", new Object[] {
				tunnelContext.getChannel().getId(),
				data.obj.getLastException(),
				data.obj.getLastTimeout(),
				data.obj.getCause() });

		// 如果没有发生过错误，则使用默认初始心跳覆盖客户端提交的建议值
		int initTimeout = timeoutPolicyFactory.getConfigurationProvider()
				.getDefault();
		if (HeartbeatException.choke.equals(data.obj.getLastException())
				|| HeartbeatException.exception.equals(data.obj
						.getLastException())) {
			// 如果出错了，就把初始心跳打对折
			initTimeout = data.obj.getLastTimeout() / 2;
		}

		tunnelContext.setCurrentTimeoutPolicy(timeoutPolicyFactory.getInstance(
				data.obj.getLastException().toString(),
				initTimeout));

		int initial =
				tunnelContext.getCurrentTimeoutPolicy()
						.getInitialTimeout();
		logger.debug("[{}]Initial heartbeat:{}", tunnelContext.getChannel()
				.getId(), initial);

		tunnelContext.getChannel().write(
				new TunnelData<MessageLite>(data.tag, 2, HeartbeatResponse
						.newBuilder()
						.setDelay(initial)
						.build()));
	}
}