package com.babeeta.butterfly.application.gateway.device;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.babeeta.butterfly.MessageSelector;
import com.babeeta.butterfly.RecipientAlreadyRegisteredException;
import com.babeeta.butterfly.application.gateway.device.push.MessagePusher;
import com.babeeta.butterfly.application.gateway.device.tunnel.TunnelService;
import com.babeeta.butterfly.router.network.Service;

public class DeviceGatewayService implements Daemon {

	private static void bootstrap(final ServerContext serverContext)
			throws Exception {

		final NioServerSocketChannelFactory nioServerSocketChannelFactory = new NioServerSocketChannelFactory(
				serverContext.bossExecutorService,
				serverContext.workerExecutorService);

		final Service messageService = new Service(
				createNewMessageSelector(serverContext),
				serverContext.workerExecutorService,
				serverContext.bossExecutorService);

		messageService.start(serverContext.messageServiceAddress);
		System.out.println("Message service started.");

		final TunnelService tunnelService = new TunnelService(
				nioServerSocketChannelFactory, serverContext);
		tunnelService.start();
		System.out.println("Tunnel service started.");
	}

	private static MessageSelector createNewMessageSelector(
			ServerContext serverContext)
			throws RecipientAlreadyRegisteredException {
		MessageSelector messageSelector = new MessageSelector();
		messageSelector.register("rpc",
				serverContext.getRPCServiceHandler());

		messageSelector.setDefaultMessageHandler(MessagePusher
				.getDefaultInstance());
		return messageSelector;
	}

	@Override
	public void destroy() {

	}

	@Override
	public void init(DaemonContext ctx) throws DaemonInitException, Exception {
		System.out.println("initializing...");
		Options options = new Options();

		Option option = new Option("r", true, "网关编号");
		option.setRequired(true);
		option.setArgName("host");
		options.addOption(option);

		try {
			CommandLine cl = new GnuParser().parse(options, ctx.getArguments());

			File DIR_LOG = new File(
					StringUtils.isBlank(
							System.getenv(("LOG_DIR"))) ?
									"/var/log/dev-gateway-service"
							: System.getenv(("LOG_DIR")));
			if (!DIR_LOG.exists()) {
				DIR_LOG.mkdirs();
			}
			System.setProperty("LOG_DIR", DIR_LOG.getCanonicalPath());

			ServerContext serverContext = new ServerContext(cl);
			bootstrap(serverContext);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("", options);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {

	}

}
