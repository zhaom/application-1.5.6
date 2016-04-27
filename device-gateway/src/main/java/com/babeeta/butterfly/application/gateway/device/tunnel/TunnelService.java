package com.babeeta.butterfly.application.gateway.device.tunnel;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.gateway.device.ServerContext;
import com.babeeta.butterfly.application.gateway.device.tunnel.let.monitor.TunnelJMX;

public class TunnelService {
	private static final Logger logger = LoggerFactory
			.getLogger(TunnelService.class);

	private static final int DEFAULT_PORT = 5757;

	public static void main(String[] args) {
		ExecutorService executorService = Executors.newCachedThreadPool();
		NioServerSocketChannelFactory nioServerSocketChannelFactory = new NioServerSocketChannelFactory(
				executorService, executorService);
		TunnelService service = new TunnelService(
				nioServerSocketChannelFactory, null);
		service.start();
	}

	private static void startMonitor() {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		TunnelJMX tunnel = new TunnelJMX();
		try {
			ObjectName tunnelObjectName = new ObjectName(
					"com.babeeta.butterfly.application.gateway.device.tunnel.let.monitor:name=TunnelJMX");
			mBeanServer.registerMBean(tunnel, tunnelObjectName);
			logger.info("TunnelJMX Server is started.");
		} catch (Exception e) {
			logger.info("TunnelJMX Server error:" + e.getMessage());
		}
	}

	private final ServerContext serverContext;
	private final NioServerSocketChannelFactory nioServerSocketChannelFactory;
	private Channel serverChannel;
	private Channel serverChannel443;

	private static ChannelGroup channelGroup = new DefaultChannelGroup();

	public static int getTunnelCount() {
		return channelGroup.size();
	}

	private final TunnelLetFactory tunnelLetFactory;

	public TunnelService(
			NioServerSocketChannelFactory nioServerSocketChannelFactory,
			ServerContext serverContext) {
		super();
		this.serverContext = serverContext;
		this.nioServerSocketChannelFactory = nioServerSocketChannelFactory;
		this.tunnelLetFactory = new DefaultTunnelLetFactory(this.serverContext);

	}

	public void shutdown() {
		serverChannel.close().awaitUninterruptibly();
		serverChannel443.close().awaitUninterruptibly();
		channelGroup.close().awaitUninterruptibly();
	}

	public void start() {
		ServerBootstrap serverBootstrap = new ServerBootstrap(
				nioServerSocketChannelFactory);
		serverBootstrap.setOption("tcpNoDelay", true);
		serverBootstrap.setPipelineFactory(new TunnelPipelineFactory(
				channelGroup, tunnelLetFactory));

		serverChannel = serverBootstrap.bind(new InetSocketAddress(
				DEFAULT_PORT));
		serverChannel443 = serverBootstrap.bind(new InetSocketAddress(443));

		logger.info("Tunnel server is serving on {} and 443",
				serverChannel.getLocalAddress());

		startMonitor();
	}
}