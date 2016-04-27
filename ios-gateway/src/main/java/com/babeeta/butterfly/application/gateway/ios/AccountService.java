package com.babeeta.butterfly.application.gateway.ios;

import java.security.KeyStore;

public interface AccountService {
	/**
	 * 按Application Id查找对应的Keystore
	 * 
	 * @param applicationId
	 * @return
	 */
	KeyStore findByApplicationId(String applicationId);

	/**
	 * Application是否处在开发沙箱模式
	 * 
	 * @return
	 */
	boolean isApplicationInDevelopment(String applicationId);
}
