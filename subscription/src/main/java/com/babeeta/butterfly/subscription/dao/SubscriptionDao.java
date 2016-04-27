package com.babeeta.butterfly.subscription.dao;

public interface SubscriptionDao {

	public void save(String aid, String cid, String did);

	public void remove(String aid, String cid);

	boolean exists(String aid, String cid);
}
