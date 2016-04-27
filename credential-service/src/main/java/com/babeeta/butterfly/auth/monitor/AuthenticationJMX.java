package com.babeeta.butterfly.auth.monitor;

import java.util.concurrent.ThreadPoolExecutor;

import com.babeeta.butterfly.auth.AuthenticationRouter;

/**
 * Created by IntelliJ IDEA. User: XYuser Date: 10-12-23 Time: 下午5:21 To change
 * this template use File | Settings | File Templates.
 */
public class AuthenticationJMX implements AuthenticationJMXMBean {
	private final ThreadPoolExecutor coreThreadPool;

	public AuthenticationJMX(ThreadPoolExecutor coreThreadPool) {
		super();
		this.coreThreadPool = coreThreadPool;
	}

	public long getAuthCount() {
		return AuthenticationRouter.AUTH_COUNT.getAndSet(0);
	}

	@Override
	public int getThreadPoolActiveCount() {
		return coreThreadPool.getActiveCount();
	}

	@Override
	public int getThreadPoolMaxPoolSize() {
		return coreThreadPool.getMaximumPoolSize();
	}

	@Override
	public int getThreadPoolQueueLength() {
		return coreThreadPool.getQueue().size();
	}
}
