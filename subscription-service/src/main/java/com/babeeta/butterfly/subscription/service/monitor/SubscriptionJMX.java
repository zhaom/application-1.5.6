package com.babeeta.butterfly.subscription.service.monitor;

import java.util.concurrent.ThreadPoolExecutor;

import com.babeeta.butterfly.subscription.service.SubscriptionHandler;

/**
 * Created by IntelliJ IDEA. User: XYuser Date: 10-12-23 Time: 下午4:12 To change
 * this template use File | Settings | File Templates.
 */
public class SubscriptionJMX implements SubscriptionJMXMBean {

	private final ThreadPoolExecutor threadPoolExecutor;

	public SubscriptionJMX(ThreadPoolExecutor threadPoolExecutor) {
		super();
		this.threadPoolExecutor = threadPoolExecutor;
	}

    public long getMessageCount() {
        return SubscriptionHandler.MESSAGE_COUNT.getAndSet(0);
    }

	@Override
	public int getThreadPoolActiveCount() {
		return threadPoolExecutor.getActiveCount();
}

	@Override
	public int getThreadPoolMaxPoolSize() {
		return threadPoolExecutor.getMaximumPoolSize();
	}

	@Override
	public int getThreadPoolQueueLength() {
		return threadPoolExecutor.getQueue().size();
	}
}
