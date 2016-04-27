package com.babeeta.butterfly.application.monitoring.http;

import org.apache.commons.cli.CommandLine;

import com.babeeta.butterfly.application.monitoring.http.monitor.HttpMonitor;

public class MonitoringContext {

	final String domain;
	final String serviceName;
	/*
	 * final int timeout; final int delay;
	 */
	final HttpMonitor monitor;

	public MonitoringContext(CommandLine commandLine) throws Exception {
		domain = commandLine.getOptionValue("i");
		/*
		 * timeout = commandLine.getOptionValue("t") == null ? 15 : Integer
		 * .valueOf(commandLine.getOptionValue("t")); delay =
		 * commandLine.getOptionValue("d") == null ? 5 : Integer
		 * .valueOf(commandLine.getOptionValue("d"));
		 */
		serviceName = commandLine.getOptionValue("s");
		monitor = MonitorFactory.getMonitor(serviceName);
		if (monitor == null) {
			throw new Exception("Unknow service :" + serviceName);
		}
	}
}
