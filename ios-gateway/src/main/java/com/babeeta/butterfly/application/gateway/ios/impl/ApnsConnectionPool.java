package com.babeeta.butterfly.application.gateway.ios.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.gateway.ios.AccountService;

public class ApnsConnectionPool extends GenericKeyedObjectPool {

	private static final Logger logger = LoggerFactory
			.getLogger(ApnsConnectionPool.class);

	public ApnsConnectionPool(AccountService accountService) {
		super(new PooledApnConnectionFactory(
				accountService));

		this.setMaxActive(8);
		this.setMaxIdle(20);
		this.setMinIdle(1);
		this.setTimeBetweenEvictionRunsMillis(10000L);
		this.setTestWhileIdle(true);
		this.setTestOnBorrow(false);
		this.setTestOnReturn(true);
	}

	@Override
	public Object borrowObject(final Object key) throws Exception {
		final ApnsConnection conn = (ApnsConnection) super.borrowObject(key);
		return Proxy.newProxyInstance(Thread.currentThread()
				.getContextClassLoader(),
				new Class<?>[] { ApnsConnection.class },
				new InvocationHandler() {

					@Override
					public Object invoke(Object proxy, Method method,
							Object[] args)
							throws Throwable {
						if ("close".equals(method.getName())) {
							logger.debug(
									"Returning connection of {} back to pool.",
									key);
							returnObject(key, conn);
							return null;
						}
				else {
					return method.invoke(conn, args);

				}
			}
				});
	}
}