package com.babeeta.butterfly.application.gateway.ios.impl;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

public class AccountServiceImplTest {

	private static final String APID = "48857174287d4dbcaf87bfc17c8bd198";
	private AccountServiceImpl serviceImpl;
	private DB db;

	@Before
	public void setUp() throws Exception {
		serviceImpl = new AccountServiceImpl();

		db = new Mongo(new ServerAddress(InetAddress.getByName("mongodb")))
				.getDB("credential_accounts_app");

		byte[] data = IOUtils
				.toByteArray(Thread
						.currentThread()
						.getContextClassLoader()
						.getResourceAsStream(
								"com/babeeta/butterfly/application/gateway/ios/impl/apns_cert.p12"));
		if (db.getCollection(db.getName()).findOne(
				new BasicDBObject("_id", APID)) == null) {
			db.getCollection(db.getName()).insert(
					new BasicDBObject("_id", APID));
		}
		if (!db.getCollection(db.getName())
				.findOne(new BasicDBObject("_id", APID)).containsField("apns")) {
			db.getCollection(db.getName()).save(
					new BasicDBObjectBuilder()
							.append("_id", APID)
							.append("apns",
									new BasicDBObjectBuilder()
											.append("development", true)
											.append("certificate", data).get())
							.get());
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFindByApplicationId() throws KeyStoreException {
		KeyStore keystore = serviceImpl
				.findByApplicationId(APID);
		assertNotNull(keystore);
		assertTrue(((X509Certificate) keystore.getCertificate(keystore
				.aliases().nextElement())).getSubjectX500Principal().getName()
				.indexOf("com.babeeta.testPush") != -1);
	}

	@Test
	public void testIsApplicationInDevelopment() {
		assertEquals(true, serviceImpl.isApplicationInDevelopment(APID));
	}
}
