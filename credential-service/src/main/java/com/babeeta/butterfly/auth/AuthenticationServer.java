package com.babeeta.butterfly.auth;

import java.lang.management.ManagementFactory;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.MessageHandler;
import com.babeeta.butterfly.auth.monitor.AuthenticationJMX;
import com.babeeta.butterfly.router.network.MessageSenderImpl;
import com.babeeta.butterfly.router.network.Service;
import com.mongodb.Mongo;

/**
 * Created by IntelliJ IDEA. User: yinchong Date: 2010-11-30 Time: 10:23:02 To
 * change this template use File | Settings | File Templates.
 */
public class AuthenticationServer {
	private final static Logger logger = LoggerFactory
			.getLogger(AuthenticationServer.class);
	private static Mongo mongo = null;

	public static void main(String[] args) {

		Options options = new Options();

		Option option = new Option("l", true, "验证服务域名");
		option.setRequired(true);
		option.setArgName("host");
		options.addOption(option);

		option = new Option("m", true, "Mongodb地址端口");
		option.setRequired(false);
		option.setArgName("host:port");
		option.setArgs(2);
		option.setValueSeparator(':');
		options.addOption(option);

		try {
			CommandLine cl = new GnuParser().parse(options, args);
			if (cl.getOptionValues("m") != null
					&& cl.getOptionValues("m").length > 0) {
				mongo = new Mongo(cl.getOptionValues("m")[0],
						Integer.parseInt(cl.getOptionValues("m")[1]));
			} else {
				mongo = new Mongo("mongodb", 27017);
			}

			bootstrap(cl.getOptionValue("l"), mongo);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java ....AuthenticationServer", options);
		}
	}

	private static void bootstrap(String address, Mongo mongo) throws Exception {
		final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
				(Runtime.getRuntime().availableProcessors() < 8 ? 8 : Runtime
						.getRuntime().availableProcessors()) * 2, 32, 15,
				TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(10240));
		MessageHandler messageHandler = new AuthenticationRouter(
				new MessageSenderImpl(), address, mongo);
		final Service service = new Service(messageHandler, executorService,
				executorService);
		service.start(address);
		startMonitor(executorService);
		Runtime.getRuntime().removeShutdownHook(new Thread("Shutdown") {
			@Override
			public void run() {
				service.shutdownGraceFully();
				executorService.shutdownNow();
			}
		});
	}

	private static void startMonitor(ThreadPoolExecutor executorService) {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		AuthenticationJMX authMBean = new AuthenticationJMX(executorService);
		try {
			ObjectName objectName = new ObjectName(
					"com.babeeta.butterfly.auth.monitor:name=AuthenticationJMX");
			mBeanServer.registerMBean(authMBean, objectName);
			logger.info("AuthenticationJMX Server is started.");
		} catch (Exception e) {
			logger.info("AuthenticationJMX Server error:" + e.getMessage());
		}
	}
}