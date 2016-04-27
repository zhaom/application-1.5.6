package com.babeeta.butterfly.application.gateway.device.tunnel;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

public class TunnelHandler extends IdleStateAwareChannelUpstreamHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(TunnelHandler.class);

	private final ChannelGroup channelGroup;
	private final TunnelLetFactory tunnelLetFactory;
	private final TunnelContext tunnelContext;

	public TunnelHandler(ChannelGroup channelGroup,
							TunnelLetFactory tunnelLetFactory) {
		this.channelGroup = channelGroup;
		this.tunnelLetFactory = tunnelLetFactory;
		tunnelContext = new TunnelContext();
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		logger.debug("[{}]Channel Closed.", e.getChannel().getId());
		super.channelClosed(ctx, e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		channelGroup.add(ctx.getChannel());
		tunnelContext.setChannel(e.getChannel());
		logger.debug("[{}]Channel Connected.", e.getChannel().getId());
	}

	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e)
			throws Exception {
		logger.info("[{}]reader Idle.", e.getChannel().getId());
		e.getChannel().close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		logger.info("[{}]exception Caught [{}]", e.getChannel().getId(),
				e.getCause());
		e.getChannel().close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (!(e.getMessage() instanceof TunnelData)) {
			return;
		}

		TunnelData<MessageLite> data = (TunnelData<MessageLite>) e
				.getMessage();

		TunnelLet<MessageLite> tunnelLet = (TunnelLet<MessageLite>) tunnelLetFactory
				.getTunnelLet(data.cmd);
		if (tunnelLet == null) {
			logger.error("Unkown command: {}", data.obj.getClass().getName());
		} else {
			logger.debug("[Tunnel command: {}] [TunnelLet: {}]", data.cmd,
					tunnelLet.getClass().getName());
			tunnelLet
					.messageReceived(
							tunnelContext,
							data);
		}
	}
}