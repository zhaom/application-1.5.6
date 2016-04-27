package com.babeeta.butterfly.subscription.service.monitor;

/**
 * Created by IntelliJ IDEA. User: XYuser Date: 10-12-23 Time: 下午4:12 To change
 * this template use File | Settings | File Templates.
 */
public interface SubscriptionJMXMBean {
    long getMessageCount();

	int getThreadPoolActiveCount();

	int getThreadPoolMaxPoolSize();

	int getThreadPoolQueueLength();
}
