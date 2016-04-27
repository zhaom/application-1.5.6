package com.babeeta.butterfly.application.router.gateway.secondary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.AbstractMessageRouter;
import com.babeeta.butterfly.MessageRouting.Message;
import com.babeeta.butterfly.misc.Address;
import com.babeeta.butterfly.MessageSender;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class GatewaySecondaryRouter extends AbstractMessageRouter {

	private static final Logger logger = LoggerFactory
			.getLogger(GatewaySecondaryRouter.class);

	private final Mongo mongo;
	final static String COLLECTION_NAME = "gatewayrouter";
	private final String dbName;

	public GatewaySecondaryRouter(String dbName, Mongo mongo,
			MessageSender messageSender) {
		super(messageSender);
		this.mongo = mongo;
		this.dbName = dbName;
	}

	@Override
	protected Message transform(Message message) {
		if (message.getTo().startsWith("update@")) {
			updateRouterTable(message.getContent().toStringUtf8(),
					message.getFrom());
			return null;
		} else {
			return transformCommonMessage(message);
		}
	}

	private String getGatewayDomain(String deviceId) {
		DBObject result = mongo.getDB(dbName)
					.getCollection(COLLECTION_NAME)
					.findOne(new BasicDBObject("_id", deviceId.trim()));
		if (result == null) {
			return null;
		} else {
			return (String) result.get("gw");
		}
	}

	private Message transformCommonMessage(Message message) {
        long startTime = System.currentTimeMillis();
		Address addr = new Address(message.getTo());
		String domain = getGatewayDomain(addr.deviceId);
        logger.debug("[{}] will go to {} ,[Time:{}ms]", new Object[]{
                message.getUid(), domain, (System.currentTimeMillis() - startTime)
        });
		if (domain != null) {
			return message.toBuilder()
						.setTo(addr.buildAddress(domain)).build();
		} else {
            logger.info("[{}]Not find gateway domain.", message.getUid());
			return null;
		}
	}

	private void updateRouterTable(String deviceId, String from) {
        long startTime = System.currentTimeMillis();
		mongo.getDB(dbName)
				.getCollection(COLLECTION_NAME)
				.save(new BasicDBObjectBuilder()
						.add("_id", deviceId)
						.add("gw",
								from.substring(from.indexOf("@") + 1,
										from.length()).trim())
						.get());
        logger.debug("[Update router: {} --> {}] [Time:{}ms]", new Object[]{
                deviceId, from, (System.currentTimeMillis() - startTime)
        });
	}

}
