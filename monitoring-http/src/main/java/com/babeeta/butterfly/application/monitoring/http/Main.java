package com.babeeta.butterfly.application.monitoring.http;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Main {

	public static void main(String[] args) {
		Options options = new Options();
		Option option = new Option("s", true,
				"检测服务{3rdapp,appaccount,devaccount},必选，三选一");
		option.setRequired(true);
		option.setArgName("serviceName");
		options.addOption(option);

		/*
		 * option = new Option("t", true, "指定检测的超时时间,单位:秒，可选,默认15秒");
		 * option.setRequired(false); option.setArgName("timeout");
		 * options.addOption(option);
		 * 
		 * option = new Option("d", true, "指定检测间隔,单位:分钟，可选，默认5分钟");
		 * option.setRequired(false); option.setArgName("delay");
		 * options.addOption(option);
		 */
		try {
			CommandLine cl = new GnuParser().parse(options, args);
			MonitoringContext context = new MonitoringContext(cl);
			start(context);
		} catch (Exception e) {
			System.out.println(e.toString());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Monitoring", options);
		}
	}

	public static void start(final MonitoringContext context) {
		/*
		 * ScheduledExecutorService scheduler = Executors
		 * .newScheduledThreadPool(1); scheduler.scheduleWithFixedDelay(new
		 * Runnable() {
		 * 
		 * @Override public void run() { context.monitor.executeHttp(); } }, 0,
		 * context.delay, TimeUnit.MINUTES);
		 */
		context.monitor.executeHttp();
	}
}
