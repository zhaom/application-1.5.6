package com.babeeta.butterfly.account.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.account.dao.AccountDao;
import com.babeeta.butterfly.account.entity.Account;
import com.babeeta.butterfly.account.service.AccountService;

public class AccountServiceImpl implements AccountService {

	private final static Logger logger = LoggerFactory
			.getLogger(AccountServiceImpl.class);

	private AccountDao accountDao;

	private boolean inApp = true;

	public AccountServiceImpl(String domain) {
		// if domain contains dev,than we to consider it in DEV
		// domain,otherwise in APP domain
		if (domain.indexOf("dev") > -1) {
			inApp = false;
		}
	}

	@Override
	public String auth(Account paramAccount) {
		Account account = accountDao.selectById(paramAccount.getId());
		if (account != null && (account.getSecureKey() != null
				&& account.getSecureKey().equals(paramAccount.getSecureKey()))) {
			if (!inApp) {
				logger.debug("Auth success, id:{},key:{}", account.getId(),
						account.getSecureKey());
				return "{\"status\":\"OK\"}";
			}
			if (inApp && "NORMAL".equals(account.getStatus())) {
				logger.debug("Auth success, id:{},key:{}", account.getId(),
						account.getSecureKey());
				return "{\"status\":\"OK\"}";
			} else {
				logger.debug(
						"Auth failed,reason for 'FREEZED' status, Id:{},key:{}",
						account.getId(),
						account.getSecureKey());
				return "{\"status\":\"FREEZED\"}";
			}
		}
		logger.debug("Auth failed, Id:{},key:{}", paramAccount.getId(),
				paramAccount.getSecureKey());
		return "{\"status\":\"FAIL\"}";
	}

	@Override
	public Account getAccount(Account account) {
		return accountDao.selectById(account.getId());
	}

	@Override
	public List<Account> ListAccount(Account account) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (account.getStatus() != null) {
			map.put("status", account.getStatus());
		}
		Map<String, Object> extraMap = account.getExtra();
		Iterator<String> it = extraMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			map.put("extra." + key, extraMap.get(key));
		}
		return accountDao.selectByQuery(map);
	}

	@Override
	public Account register(Account account) {
		if (inApp) {
			account.setStatus("FREEZED");
		}
		account.setId(UUID.randomUUID().toString().replaceAll("-", ""));
		account.setSecureKey(UUID.randomUUID().toString()
				.replaceAll("-", ""));
		account.setCreateDate(new Date());
		account = accountDao.insertAccount(account);
		logger.debug("Register result:  id:{},key:{}", account.getId(),
				account.getSecureKey());
		return account;
	}

	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	@Override
	public void updateAccount(Account account) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (account.getStatus() != null) {
			map.put("status", account.getStatus());
		}
		if (account.getExtra() != null) {
			map.put("extra", account.getExtra());
		}
		logger.debug("Update result:  id:{}", account.getId());
		accountDao.update(account.getId(), map);
	}
}
