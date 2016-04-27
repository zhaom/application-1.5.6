package com.babeeta.butterfly.subscription.service;

import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
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
import com.babeeta.butterfly.router.network.MessageSenderImpl;
import com.babeeta.butterfly.router.network.Service;
import com.babeeta.butterfly.subscription.service.monitor.SubscriptionJMX;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Created by IntelliJ IDEA. User: yinchong Date: 2010-12-1 Time: 16:59:08 To
 * change this template use File | Settings | File Templates.
 */
public class SubscriptionServer {
	private final static Logger logger = LoggerFactory
			.getLogger(SubscriptionServer.class);
	private static final String SUB_DOMAIN = "subscription.dev";
	private static Mongo mongo;

	public static void main(String[] args) throws UnknownHostException,
			MongoException {

		Options options = new Options();

		Option option = new Option("l", true, "推送订阅服务域名");
		option.setRequired(false);
		option.setArgName("host");
		options.addOption(option);

		option = new Option("m", true, "MongoDB地址端口");
		option.setRequired(false);
		option.setArgName("host:port");
		option.setArgs(2);
		option.setValueSeparator(':');
		options.addOption(option);

		try {
			CommandLine cl = new GnuParser().parse(options, args);

			if (cl.getOptionValues("m") == null
					|| cl.getOptionValues("m").length == 0) {
				mongo = new Mongo("mongodb", 27017);
			} else {
				mongo = new Mongo(cl.getOptionValues("m")[0],
						Integer.parseInt(cl.getOptionValues("m")[1]));
			}

			if (cl.getOptionValue('l') == null
					|| cl.getOptionValue("l").trim().length() == 0) {
				bootstrap(SUB_DOMAIN, mongo);
			} else {
				bootstrap(cl.getOptionValue("l"), mongo);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java ....SubscriptionServer", options);
		}

	}

	private static void bootstrap(String host, Mongo mongo) throws Exception {
		final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
				(Runtime.getRuntime().availableProcessors() < 8 ? 8 : Runtime
						.getRuntime().availableProcessors()) * 2, 32, 15,
				TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(10240));
		MessageHandler messageHandler = new SubscriptionHandler(
				new MessageSenderImpl(), mongo);
		final Service service = new Service(messageHandler, executorService,
				executorService);

		service.start(host);

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
		SubscriptionJMX subMBean = new SubscriptionJMX(executorService);
		try {
			ObjectName objectName = new ObjectName(
					"com.babeeta.butterfly.subscription.service.monitor:name=SubscriptionJMX");
			mBeanServer.registerMBean(subMBean, objectName);
			logger.info("SubscriptionJMX Server is started.");
		} catch (Exception e) {
			logger.info("SubscriptionJMX Server error:" + e.getMessage());
		}
	}
}
