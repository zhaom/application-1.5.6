package com.babeeta.butterfly.auth;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.slf4j.*;

import com.babeeta.butterfly.*;
import com.google.protobuf.*;
import com.mongodb.*;

/**
 * Created by IntelliJ IDEA. User: yinchong Date: 2010-11-30 Time: 10:27:50 To
 * change this template use File | Settings | File Templates.
 */
public class AuthenticationRouter extends AbstractMessageRouter {

	public String AUTH_ACCOUNT_APP;
	public String REGISTER_ACCOUNT_APP;

	private final static Logger logger = LoggerFactory
			.getLogger(AuthenticationRouter.class);
	private final Mongo MONGO;
	private final String DB_NAME;

	public static AtomicLong AUTH_COUNT = new AtomicLong(0);

	public AuthenticationRouter(MessageSender messageSender, String host,
			Mongo mongo) {
		super(messageSender);
		AUTH_ACCOUNT_APP = "auth@" + host;
		REGISTER_ACCOUNT_APP = "register@" + host;
		MONGO = mongo;
		DB_NAME = "credential_" + host.replaceAll("\\.", "_");
	}

	public MessageRouting.Message authentication(MessageRouting.Message message) {
		long startTime = System.currentTimeMillis();
		boolean isNotEmpty = (message.getContent() != null && message
				.getContent().toStringUtf8().indexOf(":") > -1);
		String[] content = isNotEmpty ? message.getContent().toStringUtf8()
				.split(":") : new String[] { "", "" };
		DBObject dbObject = getDBCollection().findOne(
				new BasicDBObject("_id", content[0]));

		String key = (dbObject != null ? (String) dbObject.get("key") : null);
		String authResult = content[1].equals(key) ? "OK" : "FAIL";

		String strUUID = java.util.UUID.randomUUID().toString()
				.replaceAll("-", "");
		MessageRouting.Message authMsg = MessageRouting.Message.newBuilder()
				.setUid(strUUID).setDate(message.getDate())
				.setFrom(AUTH_ACCOUNT_APP).setTo(message.getFrom())
				.setContent(ByteString.copyFromUtf8(authResult))
				.setReplyFor(message.getUid()).setExpire(message.getExpire())
				.build();

		logger.debug("[Auth result:{}] [Key:{}] [Time:{}ms]", new Object[] {
				authResult, key, (System.currentTimeMillis() - startTime) });
		return authMsg;
	}

	public MessageRouting.Message register(MessageRouting.Message message) {
		long startTime = System.currentTimeMillis();
		DBObject registerInfo = null;
		try {
			MessageRouting.DeviceRegister deviceRegister = MessageRouting.DeviceRegister
					.parseFrom(message.getContent().toByteArray());

			if (deviceRegister != null) {
				registerInfo = new BasicDBObject();
				registerInfo.put("charset", deviceRegister.getCharset());
				registerInfo.put("client_version",
						deviceRegister.getClientVersion());
				registerInfo.put("imei", deviceRegister.getImei());
				registerInfo.put("os_name", deviceRegister.getOsName());
				registerInfo.put("os_version", deviceRegister.getOsVersion());
				registerInfo.put("screen_color_depth",
						deviceRegister.getScreenColorDepth());
				registerInfo.put("screen_height",
						deviceRegister.getScreenHeight());
				registerInfo.put("screen_width",
						deviceRegister.getScreenWidth());
				registerInfo.put("etc", deviceRegister.getEtc());

			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("MessageRouting.DeviceRegister parse error: {}"
					+ e.getMessage());
		}

		String id = UUID.randomUUID().toString().replaceAll("-", "");
		String key = UUID.randomUUID().toString().replaceAll("-", "");

		DBObject register = new BasicDBObject();
		register.put("_id", id);
		register.put("key", key);
		if (registerInfo != null) {
			register.put("registerInfo", registerInfo);
			logger.debug("|" + id + "|" + key + "|" + registerInfo.get("imei")
					+ "|" + registerInfo.get("client_version") + "|"
					+ registerInfo.get("os_name") + "|"
					+ registerInfo.get("os_version") + "|");
		}

		getDBCollection().insert(register);
		logger.debug("[Register result:{}] [Time:{}ms]", id + "." + key,
				(System.currentTimeMillis() - startTime));

		MessageRouting.Message registerMsg = MessageRouting.Message
				.newBuilder().setUid(id).setDate(message.getDate())
				.setFrom(REGISTER_ACCOUNT_APP).setTo(message.getFrom())
				.setContent(ByteString.copyFromUtf8(id + ":" + key))
				.setReplyFor(message.getUid()).setExpire(message.getExpire())
				.build();
		return registerMsg;
	}

	@Override
	protected MessageRouting.Message transform(MessageRouting.Message message) {
		AUTH_COUNT.getAndIncrement();
		String source = (message.getTo() != null && message.getTo()
				.indexOf("@") > -1) ? message.getTo().split("@")[0] : null;

		if ("register".equals(source)) {
			return register(message);
		} else if ("auth".equals(source)) {
			return authentication(message);
		} else {
			return null;
		}
	}

	private DBCollection getDBCollection() {
		DB db = MONGO.getDB(DB_NAME);
		return db.getCollection(DB_NAME);
	}
}
