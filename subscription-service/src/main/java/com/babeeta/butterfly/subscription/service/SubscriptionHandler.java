package com.babeeta.butterfly.subscription.service;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.AbstractMessageRouter;
import com.babeeta.butterfly.MessageRouting;
import com.babeeta.butterfly.MessageSender;
import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * Created by IntelliJ IDEA. User: yinchong Date: 2010-12-1 Time: 17:01:47 To
 * change this template use File | Settings | File Templates.
 */
public class SubscriptionHandler extends AbstractMessageRouter {
	private final static Logger logger = LoggerFactory
			.getLogger(SubscriptionHandler.class);

	private final Mongo mongo;
	static final String DB_NAME = "subscription";
	public static final String UNBIND_SUBSCRIPTION_DEV = "unbind@subscription.dev";
	public static final String BIND_SUBSCRIPTION_DEV = "bind@subscription.dev";

	public static AtomicLong MESSAGE_COUNT = new AtomicLong(0);// 记录消息数量

	public SubscriptionHandler(MessageSender messageSender, Mongo mongo) {
		super(messageSender);
		this.mongo = mongo;
	}

	@Override
	protected MessageRouting.Message transform(MessageRouting.Message message) {
		MESSAGE_COUNT.getAndIncrement();
		String source = (message.getTo() != null && message.getTo()
				.indexOf("@") > -1) ? message.getTo().split("@")[0] : null;

		if ("bind".equals(source)) {
			return bindService(message);
		} else if ("unbind".equals(source)) {
			return unbindService(message);
		} else {
			return null;
		}
	}

	/**
	 * 绑定订阅服务
	 * 
	 * @param message
	 * @return
	 */
	private MessageRouting.Message bindService(MessageRouting.Message message) {
		long startTime = System.currentTimeMillis();
		String content = message.getContent().toStringUtf8();
		if (content == null || content.indexOf(":") == -1) {
			return message;
		}

		String date = String.valueOf(System.currentTimeMillis());
		String[] contentArray = content.split(":");// aid,cid,did
		int flag = contentArray.length;// 1=aid; 2=aid,cid; 3=aid,cid,did
		if (flag != 3) {
			return null;
		}

		String strUUID = null;
		if (contentArray[1].trim().length() == 0
				|| !exists(contentArray[0], contentArray[1])) {// 没有clientId，新生成
			strUUID = UUID.randomUUID().toString().replaceAll("-", "");
		} else {
			strUUID = contentArray[1];
		}

		boolean successful = save(contentArray[0], strUUID, contentArray[2],
				date);
		logger.debug("Service bind by client [{}] [Time:{}]", strUUID,
				(System.currentTimeMillis() - startTime));
		if (successful) {
			String replyContent = new StringBuilder()
					.append(contentArray[0]).append(":")
					.append(strUUID).append(":")
					.append(contentArray[2]).toString();
			MessageRouting.Message msg = MessageRouting.Message.newBuilder()
					.setContent(ByteString.copyFromUtf8(replyContent))
					.setTo(message.getFrom())
					.setUid(UUID.randomUUID().toString().replaceAll("-", ""))
					.setReplyFor(message.getUid())
					.setFrom(BIND_SUBSCRIPTION_DEV)
					.setDate(message.getDate())
					.build();
			return msg;
		} else {
			return null;
		}
	}

	private boolean exists(String aid, String cid) {
		DBObject obj = new BasicDBObjectBuilder()
				.add("key", new BasicDBObjectBuilder()
						.add("aid", aid)
						.add("cid", cid)
						.get())
					.get();
		return this.getDBCollection().findOne(obj) != null;
	}

	private DBCollection getDBCollection() {
		DB db = mongo.getDB(DB_NAME);
		return db.getCollection(DB_NAME);
	}

	private boolean remove(String aid, String cid) {
		BasicDBObject key = new BasicDBObject();
		key.put("aid", aid);
		key.put("cid", cid);

		BasicDBObject doc = new BasicDBObject();
		doc.put("_id", key);

		this.getDBCollection().remove(doc);
		return true;
	}

	private boolean save(String aid, String cid, String did, String date) {
		BasicDBObject key = new BasicDBObject();
		key.put("aid", aid);
		key.put("cid", cid);

		DBObject deviceId = new BasicDBObject();
		deviceId.put("did", did);

		BasicDBObject doc = new BasicDBObject();
		doc.put("_id", key);
		doc.putAll(deviceId);
		doc.put("date", date);

		// this.getDBCollection().createIndex(deviceId);
		this.getDBCollection().save(doc);
		return true;
	}

	/**
	 * 解绑推送服务
	 * 
	 * @param message
	 * @return
	 */
	private MessageRouting.Message unbindService(MessageRouting.Message message) {
		long startTime = System.currentTimeMillis();
		String content = message.getContent().toStringUtf8();
		if (content == null || content.indexOf(":") == -1) {
			return message;
		}

		String aid = content.split(":")[0];
		String cid = content.split(":")[1];

		boolean isOK = remove(aid, cid);
		MessageRouting.Message msg = MessageRouting.Message.newBuilder()
				.setContent(ByteString.copyFromUtf8(isOK ? "OK" : "FAIL"))
				.setTo(message.getFrom())
				.setUid(UUID.randomUUID().toString().replaceAll("-", ""))
				.setReplyFor(message.getUid())
				.setFrom(UNBIND_SUBSCRIPTION_DEV)
				.setDate(message.getDate())
				.build();
		logger.debug("Service unbind by client [{}] [Time:{}]", cid,
				(System.currentTimeMillis() - startTime));
		return msg;
	}
}
