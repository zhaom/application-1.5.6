package com.babeeta.butterfly.auth;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;

/**
 * Created by IntelliJ IDEA. User: yinchong Date: 2010-11-27 Time: 15:51:17 To
 * change this template use File | Settings | File Templates.
 */
public class TestMongoDB {

	private static final Logger logger = LoggerFactory
			.getLogger(TestMongoDB.class);

	static String mongodbAddress = "mongodb";
	// static String mongodbAddress = "192.168.20.136";
	static String AID = "44bbcd579c294406b0099df1362b472d";
	static String CID = "5c3f7ffadbe34c6faf6394a7f3114d11";
	static String DID = "8e174a4dd48a4aa08341bf3d3b16cafa";
	static String KEY = "2350b2fed2b2409ba15acff2841c8b5c";

	@Test
	public void auth_app() throws Exception {

		Mongo mongo = new Mongo(mongodbAddress, 27017);
		DB db = mongo.getDB("credential_accounts_app");
		DBCollection dbCollection = db.getCollection("credential_accounts_app");

		DBObject register = new BasicDBObject();
		register.put("_id", AID);
		register.put("key", KEY);

		dbCollection.insert(register);

		DBObject dbObject = dbCollection.findOne(new BasicDBObject("_id", AID));
		assertNotNull(dbObject);
		assertEquals(AID, String.valueOf(dbObject.get("_id")));
		System.out.println("[auth_app]" + dbObject.toString());
	}

	@Test
	public void auth_dev() throws Exception {

		Mongo mongo = new Mongo(mongodbAddress, 27017);
		DB db = mongo.getDB("credential_accounts_dev");
		DBCollection dbCollection = db.getCollection("credential_accounts_dev");

		DBObject register = new BasicDBObject();
		register.put("_id", AID);
		register.put("key", KEY);

		dbCollection.insert(register);

		DBObject dbObject = dbCollection.findOne(new BasicDBObject("_id", AID));
		assertNotNull(dbObject);
		assertEquals(AID, String.valueOf(dbObject.get("_id")));
		System.out.println("[auth_dev]" + dbObject.toString());
	}

	@Test
	public void dev() throws Exception {

		final String FIELD__ID = "_id";
		final String FIELD_DEVICE_ID = "did";
		final String FIELD_CLIENT_ID = "cid";
		final String FIELD_APPLICATION_ID = "aid";
		final String DB_NAME = "subscription";

		Mongo mongo = new Mongo(mongodbAddress, 27017);
		DB db = mongo.getDB(DB_NAME);
		DBCollection dbCollection = db.getCollection(DB_NAME);
		DBObject b = new BasicDBObject(
				FIELD__ID,
				new BasicDBObjectBuilder()
						.add(FIELD_APPLICATION_ID, AID)
						.add(FIELD_CLIENT_ID, CID)
						.get());
		b.put(FIELD_DEVICE_ID, DID);
		dbCollection.insert(b);

		DBObject result = dbCollection.findOne(b);
		assertNotNull(result);
		assertEquals(DID, String.valueOf(result.get(FIELD_DEVICE_ID)));
		System.out.println("[dev]" + result.get(FIELD_DEVICE_ID));
	}

	@Test
	public void gateway0() throws Exception {

		final String FIELD__ID = "_id";
		final String FIELD_GW_ID = "gw";
		final String DB_NAME = "gatewayrouter0";

		Mongo mongo = new Mongo(mongodbAddress, 27017);
		DB db = mongo.getDB(DB_NAME);
		DBCollection dbCollection = db.getCollection("gatewayrouter");
		BasicDBObject b0 = new BasicDBObject();
		b0.put(FIELD__ID, DID);
		b0.put(FIELD_GW_ID, "0.gateway.dev");

		dbCollection.insert(b0);

		DBObject result = dbCollection.findOne(b0);
		assertNotNull(result);
		assertEquals("0.gateway.dev", String.valueOf(result.get(FIELD_GW_ID)));
		System.out.println("[gateway0]" + result.get(FIELD_GW_ID));
	}

	@Test
	public void gateway1() throws Exception {

		final String FIELD__ID = "_id";
		final String FIELD_GW_ID = "gw";
		final String DB_NAME = "gatewayrouter1";

		Mongo mongo = new Mongo(mongodbAddress, 27017);
		DB db = mongo.getDB(DB_NAME);
		DBCollection dbCollection = db.getCollection("gatewayrouter");
		BasicDBObject b1 = new BasicDBObject();
		b1.put(FIELD__ID, DID);
		b1.put(FIELD_GW_ID, "1.gateway.dev");

		WriteResult wr = dbCollection.insert(b1);
		// System.out.println(wr.getError());

		DBObject result = dbCollection.findOne(b1);
		assertNotNull(result);
		assertEquals("1.gateway.dev", String.valueOf(result.get(FIELD_GW_ID)));
		System.out.println("[gateway1]" + result.get(FIELD_GW_ID));
	}
}
