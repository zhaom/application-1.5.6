package com.babeeta.butterfly.account.dao.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.babeeta.butterfly.account.dao.AccountDao;
import com.babeeta.butterfly.account.entity.Account;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class AccountDaoImpl extends BasicDaoImpl implements AccountDao {

	private String domain;

	public AccountDaoImpl(String domain) {
		this.domain = domain;
		datastore = morphia.createDatastore(mongo, domain);
		datastore.ensureIndexes();
	}

	@Override
	public void deleteById(String id) {
		Account account = new Account();
		account.setId(id);
		datastore.delete(account);
	}

	public String getDomain() {
		return domain;
	}

	@Override
	public Account insertAccount(Account account) {
		// TODO Auto-generated method stub
		datastore.save(account);
		return account;
	}

	@Override
	public Account selectById(String id) {
		Account account = new Account();
		account.setId(id);
		account = datastore.get(account);
		return account;
	}

	@Override
	public List<Account> selectByQuery(Map<String, Object> map) {
		Query<Account> query = datastore.createQuery(Account.class);
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			Object value = map.get(key);
			query.filter(key, value);
		}
		return query.asList();
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Override
	public void update(String id, Map<String, Object> map) {
		// UpdateOption
		Iterator<String> it = map.keySet().iterator();
		Query<Account> query = datastore.createQuery(Account.class).filter(
				"_id", id);
		UpdateOperations<Account> ops = datastore
				.createUpdateOperations(Account.class);
		while (it.hasNext()) {
			String key = it.next();
			Object value = map.get(key);
			ops.set(key, value);
		}
		datastore.update(query, ops, false);
	}
}
