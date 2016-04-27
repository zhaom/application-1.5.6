package com.babeeta.butterfly.subscription.dao.impl;

import java.util.Date;

import com.babeeta.butterfly.subscription.dao.SubscriptionDao;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class SubscriptionDaoImpl extends BasicDaoImpl implements
		SubscriptionDao {

	private static final String DB_NAME = "subscription";

	@Override
	public boolean exists(String aid, String cid) {
		DBObject obj = new BasicDBObjectBuilder()
				.add("_id", new BasicDBObjectBuilder()
						.add("aid", aid)
						.add("cid", cid)
						.get())
					.get();
		return mongo.getDB(DB_NAME).getCollection(DB_NAME).findOne(obj) != null;
	}

	@Override
	public void save(String aid, String cid, String did) {
		BasicDBObject key = new BasicDBObject();
		key.put("aid", aid);
		key.put("cid", cid);

		DBObject deviceId = new BasicDBObject();
		deviceId.put("did", did);

		BasicDBObject doc = new BasicDBObject();
		doc.put("_id", key);
		doc.putAll(deviceId);
		doc.put("date", new Date());

		mongo.getDB(DB_NAME).getCollection(DB_NAME).save(doc);
	}

	@Override
	public void remove(String aid, String cid) {
		BasicDBObject key = new BasicDBObject();
		key.put("aid", aid);
		key.put("cid", cid);
		BasicDBObject doc = new BasicDBObject();
		doc.put("_id", key);
		mongo.getDB(DB_NAME).getCollection(DB_NAME).remove(doc);
	}

}
