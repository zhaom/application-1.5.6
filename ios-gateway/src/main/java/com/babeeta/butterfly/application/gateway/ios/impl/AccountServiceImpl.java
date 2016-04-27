package com.babeeta.butterfly.application.gateway.ios.impl;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.gateway.ios.AccountService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

public class AccountServiceImpl implements AccountService {

	private static final Logger logger = LoggerFactory
			.getLogger(AccountServiceImpl.class);

	private final Mongo mongo;

	public AccountServiceImpl() throws UnknownHostException, MongoException {
		super();
		this.mongo = new Mongo(new ServerAddress(
				InetAddress.getByName("mongodb")));
	}

	@Override
	public KeyStore findByApplicationId(String applicationId) {
		DBObject result = mongo.getDB("credential_accounts_app")
				.getCollection("credential_accounts_app")
				.findOne(new BasicDBObject("_id", applicationId));

		if (result != null && result.containsField("apns")
				&& ((DBObject) result.get("apns")).containsField("certificate")) {
			KeyStore keystore;
			try {
				keystore = KeyStore.getInstance("PKCS12");
				keystore.load(
						new ByteArrayInputStream((byte[]) ((DBObject) result
								.get("apns"))
								.get("certificate")), DigestUtils
								.md5Hex(applicationId).toCharArray());

				if (keystore.aliases().hasMoreElements()) {
					return keystore;
				} else {
					return null;
				}
			} catch (Exception e) {
				logger.error(
						"Error ocurred while parsing certificate of {}.{}",
						applicationId, e.getMessage());
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public boolean isApplicationInDevelopment(String applicationId) {
		DBObject result = mongo.getDB("credential_accounts_app")
				.getCollection("credential_accounts_app")
				.findOne(new BasicDBObject("_id", applicationId));
		return result != null
				&& (Boolean) ((DBObject) result.get("apns")).get("development");
	}

}
