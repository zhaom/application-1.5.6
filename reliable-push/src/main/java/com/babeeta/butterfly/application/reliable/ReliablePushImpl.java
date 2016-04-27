package com.babeeta.butterfly.application.reliable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;

import com.babeeta.butterfly.MessageRouting;
import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * Created by IntelliJ IDEA. User: XYuser Date: 11-1-22 Time: 下午2:10 To change
 * this template use File | Settings | File Templates.
 */
public class ReliablePushImpl implements ReliablePush {
	static {
		try {
			mongo = new Mongo("mongodb", 27017);
		} catch (Exception e) {
		}
	}

	private static final ReliablePushImpl defaultInstance = new ReliablePushImpl();

	public static ReliablePushImpl getDefaultInstance() {
		return defaultInstance;
	}

	private final String RELIABLE_DB_NAME = "reliable_push";
	private final String SUBSCRIPTION_DB_NAME = "subscription";
	private final String STATUS_DELIVERING = "DELIVERING";// 投递中，未ack
	private final String STATUS_ACKED = "ACKED";// 已act

	private final String STATUS_EXPIRED = "EXPIRED";// 已过期

	private final String STATUS_EXPIRED_ACKED = "EXPIRED_ACKED";// 过期且已经ack

	private static Mongo mongo;

	private ReliablePushImpl() {
	}

	@Override
	public List<MessageRouting.Message> getMessagesList(String did) {
		DBObject deviceId = new BasicDBObject();
		deviceId.put("did", did);

		DBCursor keyCursor = getSubscriptionDBCollection().find(deviceId);
		List<MessageRouting.Message> list = new ArrayList<MessageRouting.Message>();

		Set<String> set = new HashSet<String>();
		while (keyCursor.hasNext()) {
			// 获取subscription数据库中的aid、cid集合
			DBObject dbObject = keyCursor.next();
			BasicDBObject idObj = (BasicDBObject) dbObject.get("_id");
			String aid = idObj.get("aid").toString();
			String cid = idObj.get("cid").toString();

			if (set.contains(aid + "." + cid)) {// 去掉重复的aid和cid组合
				continue;
			}

			// 查询Message DB中的消息
			DBObject query = new BasicDBObject();
			query.put("key",
					new BasicDBObjectBuilder().add("aid", aid).add("cid", cid)
							.get());
			query.put("status", STATUS_DELIVERING);

			DBCursor messageCursor = getReliableDBCollection().find(query);
			while (messageCursor.hasNext()) {
				DBObject result = messageCursor.next();
				String uid = result.get("_id").toString();
				if (expired(result)) {
					updateExpire(uid);
				} else {
					byte[] messageBody = (byte[]) result.get("message");

					MessageRouting.Message message = MessageRouting.Message
							.newBuilder()
							.setUid(uid)
							.setContent(ByteString.copyFrom(messageBody))
							.setFrom("reliable_push@dev")
							.setTo(new StringBuilder(cid).append(".")
									.append(aid)
									.append("@dev").toString())
							.setDate(System.currentTimeMillis()).build();
					list.add(message);
				}
			}
			set.add(aid + "." + cid);
		}
		return list;
	}

	@Override
	public String getMessageStatus(String uid) {
		DBObject messageObj = new BasicDBObject();
		messageObj.put("_id", uid);
		DBObject result = getReliableDBCollection().findOne(messageObj);

		if (result == null) {
			return null;
		} else {
			if (result.get("status").equals(STATUS_DELIVERING)
					&& expired(result)) {
				updateExpire(uid);
				result = getReliableDBCollection().findOne(messageObj);
			}
			return String.valueOf(result.get("status"));
		}
	}

	@Override
	public boolean saveMessage(MessageRouting.Message message, String aid,
								String cid) {
		DBObject key = new BasicDBObject();
		key.put("aid", aid);
		key.put("cid", cid);

		DBObject messageObj = new BasicDBObject();
		messageObj.put("_id", message.getUid());
		messageObj.put("key", key);
		messageObj.put("message", message.getContent().toByteArray());
		messageObj.put("status", STATUS_DELIVERING);
		messageObj.put("age", message.getExpire());
		messageObj.put("createdAt", new Date());

		// this.getReliableDBCollection().ensureIndex(key);
		getReliableDBCollection().save(messageObj);

		return true;
	}

	@Override
	public boolean updateAck(String uid) {
		DBObject messageObj = new BasicDBObject();
		messageObj.put("_id", uid);

		DBObject result = getReliableDBCollection().findOne(messageObj);
		// 对于已经过期的消息，改为expired_acked
		if (STATUS_EXPIRED.equals(result.get("status"))) {
			result.put("status", STATUS_EXPIRED_ACKED);
		} else {
			result.put("status", STATUS_ACKED);
		}
		result.put("ackedAt", new Date());
		getReliableDBCollection().save(result);
		return true;
	}

	@Override
	public void updateExpire(String uid) {
		DBObject messageObj = new BasicDBObject();
		messageObj.put("_id", uid);

		DBObject result = getReliableDBCollection().findOne(messageObj);
		if (result == null) {
			return;
		}
		// 只更改未ack的消息
		if (STATUS_DELIVERING.equals(result.get("status"))) {
			result.put("status", STATUS_EXPIRED);
			result.put("expiredAt", new Date());
			getReliableDBCollection().save(result);
		}
	}

	private boolean expired(DBObject result) {
		if (result.containsField("msgExptime")) {
			// 旧数据
			int exptime = ((Number) result.get("msgExptime")).intValue();
			try {
				return DateUtils.addSeconds(
						DateUtils.parseDate(result.get("saveTime").toString(),
								new String[] { "yyyy-MM-dd HH:mm:ss" }),
						exptime)
								.before(new Date());
			} catch (ParseException ignore) {
				return true;
			}
		} else if (result.containsField("age")) {
			// 新数据
			return DateUtils.addSeconds((Date) result.get("createdAt"),
					((Number) result.get("age")).intValue()).before(new Date());
		} else {
			return true;
		}
	}

	private DBCollection getReliableDBCollection() {
		DB db = mongo.getDB(RELIABLE_DB_NAME);
		return db.getCollection(RELIABLE_DB_NAME);
	}

	private DBCollection getSubscriptionDBCollection() {
		DB db = mongo.getDB(SUBSCRIPTION_DB_NAME);
		return db.getCollection(SUBSCRIPTION_DB_NAME);
	}
}
