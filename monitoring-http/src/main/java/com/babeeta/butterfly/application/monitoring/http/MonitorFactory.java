package com.babeeta.butterfly.application.monitoring.http;

import java.util.HashMap;

import com.babeeta.butterfly.application.monitoring.http.monitor.AccountMonitor;
import com.babeeta.butterfly.application.monitoring.http.monitor.HttpMonitor;
import com.babeeta.butterfly.application.monitoring.http.monitor.ThirdAppMonitor;

public class MonitorFactory {

	private static HashMap<String, HttpMonitor> monitorMap = new HashMap<String, HttpMonitor>();

	static {
		monitorMap.put("3rdapp", new ThirdAppMonitor());
		monitorMap.put("appaccount", new AccountMonitor("accounts.app"));
		monitorMap.put("devaccount", new AccountMonitor("accounts.dev"));
	}

	public static HttpMonitor getMonitor(String service) {
		return monitorMap.get(service);
	}

}
