package com.babeeta.butterfly.application.gateway.device.tunnel.let;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.MessageRouting.Heartbeat;
import com.babeeta.butterfly.MessageRouting.HeartbeatResponse;
import com.babeeta.butterfly.application.gateway.device.tunnel.TunnelContext;
import com.babeeta.butterfly.application.gateway.device.tunnel.TunnelData;
import com.babeeta.butterfly.application.gateway.device.tunnel.TunnelLet;
import com.google.protobuf.MessageLite;

import java.util.concurrent.atomic.AtomicLong;

public class HeartbeatTunnelLet implements TunnelLet<Heartbeat> {

	private final static Logger logger = LoggerFactory
			.getLogger(HeartbeatInitTunnelLet.class);

    public static AtomicLong HEARTBEAT_COUNT = new AtomicLong(0);

	@Override
	public void messageReceived(
			TunnelContext tunnelContext,
			TunnelData<Heartbeat> data) {
        HEARTBEAT_COUNT.getAndIncrement();
		tunnelContext.setCurrentTimeoutPolicy(tunnelContext
				.getCurrentTimeoutPolicy().getNextTimeoutPolicy());
		int delay =
				tunnelContext.getCurrentTimeoutPolicy()
						.getTimeout();

		logger.debug("[{}]{}", tunnelContext.getChannel().getId(), delay);
		tunnelContext.getChannel().write(
				new TunnelData<MessageLite>(data.tag, 2, HeartbeatResponse
						.newBuilder()
						.setDelay(delay)
						.build()));
	}

}
