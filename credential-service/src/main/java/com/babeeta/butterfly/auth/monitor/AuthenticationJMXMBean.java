package com.babeeta.butterfly.auth.monitor;

/**
 * Created by IntelliJ IDEA. User: XYuser Date: 10-12-23 Time: 下午5:22 To change
 * this template use File | Settings | File Templates.
 */
public interface AuthenticationJMXMBean {

	long getAuthCount();

	int getThreadPoolActiveCount();

	int getThreadPoolMaxPoolSize();

	int getThreadPoolQueueLength();
}
