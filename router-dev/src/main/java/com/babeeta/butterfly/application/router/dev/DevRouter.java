package com.babeeta.butterfly.application.router.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.AbstractMessageRouter;
import com.babeeta.butterfly.MessageRouting.Message;
import com.babeeta.butterfly.MessageSender;
import com.babeeta.butterfly.misc.Address;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import java.util.concurrent.atomic.AtomicLong;

public class DevRouter extends AbstractMessageRouter {

    static final String FIELD__ID = "_id";

    static final String FIELD_DEVICE_ID = "did";

    static final String FIELD_CLIENT_ID = "cid";

    static final String FIELD_APPLICATION_ID = "aid";

    static final String DB_NAME = "subscription";

    private final Mongo mongo;

    private static final Logger logger = LoggerFactory
            .getLogger(DevRouter.class);

    public static AtomicLong MESSAGE_COUNT = new AtomicLong(0);

    public DevRouter(MessageSender messageSender, Mongo mongo) {
        super(messageSender);
        this.mongo = mongo;
    }

    @Override
    protected Message transform(Message message) {
        long startTime = System.currentTimeMillis();
        MESSAGE_COUNT.getAndIncrement();
        Address addr = new Address(message.getTo());
        String deviceId = findDeviceId(addr, message);

        if (deviceId != null) {
            String transformed = new StringBuilder(deviceId)
                    .append(".")
                    .append(addr.clientId)
                    .append(".")
                    .append(addr.applicationId)
                    .append("@gateway.dev")
                    .toString();
            logger.debug("[{}]Transform:[{}] --> [{}], Time:{}ms", new Object[]{
                    message.getUid(),
                    message.getTo(),
                    transformed,
                    (System.currentTimeMillis() - startTime)});
            return message.toBuilder().setTo(transformed).build();
        } else {
            logger.debug("[{}]No suitable device. Message has been dropped.",
                    message.getUid());
            return null;
        }
    }

    private String findDeviceId(Address addr, Message message) {
        DB db = mongo.getDB(DB_NAME);
        DBCollection dbCollection = db.getCollection(DB_NAME);
        DBObject result = dbCollection
                .findOne(new BasicDBObject(
                        FIELD__ID,
                        new BasicDBObjectBuilder()
                                .add(FIELD_APPLICATION_ID, addr.applicationId)
                                .add(FIELD_CLIENT_ID, addr.clientId)
                                .get()));
        if (result != null) {
            return (String) result.get(FIELD_DEVICE_ID);
        } else {
            logger.info("[{}]Not find device id.", message.getUid());
            return null;
        }
    }
}
