package com.babeeta.butterfly.application.app.schedule;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.app.MessageContext;
import com.babeeta.butterfly.application.app.pusher.MessagePusher;
import com.babeeta.butterfly.application.app.record.MessageRecordService;
import com.babeeta.butterfly.application.app.record.entity.MessageRecord;

/****
 * 
 * @update zeyong.xia  add update executeAt
 * @date 2011-9-21
 */
public class ScheduledMessageTask implements Runnable, Delayed {
	private final static Logger logger = LoggerFactory
			.getLogger(ScheduledMessageTask.class);
	private String uid;
	private long createAt;
	private long executeAt;

	public ScheduledMessageTask(String uid, int delayTimeInMinute) {
		this.uid = uid;
		createAt = new Date().getTime();
		executeAt = createAt + delayTimeInMinute * 60 * 1000;
	}

	public String getUid() {
		return uid;
	}

	@Override
	public int compareTo(Delayed o) {
		ScheduledMessageTask that = (ScheduledMessageTask) o;
		return executeAt > that.executeAt ? 1
				: (executeAt < that.executeAt ? -1 : 0);
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(executeAt - createAt, unit);
	}

	@Override
	public void run() {
		MessageRecord messageRecord = MessageRecordService.getDefaultInstance()
				.getDao().getMessageRecordbyId(uid);

		if (messageRecord != null) {
			MessageContext mtx = new MessageContext();
			mtx.setMessageId(messageRecord.getMessageId());
			mtx.setParentId(messageRecord.getMessageId());
			mtx.setSender(messageRecord.getAppId());
			mtx.setRecipient(messageRecord.getRecipient());
			mtx.setDataType(messageRecord.getDataType());
			mtx.setExpire(messageRecord.getExpire());
			mtx.setDelay(0);
			mtx.setContent(messageRecord.getContent());
			mtx.setBroadcastFlag(messageRecord.getBroadcastFlag());

			if (messageRecord.getStatus().equals("DELAYING")) {
				if (messageRecord.getBroadcastFlag()) {
					MessagePusher.getDefaultInstance().broadcast(mtx);
				} else {
					MessagePusher.getDefaultInstance().unicast(
							messageRecord.getRecipient(), mtx);
				}
			} else {
				logger.info("Delay message {} is in status {}.", uid,
						messageRecord.getStatus());
			}
		} else {
			logger.info("Not found delay message {}.", uid);
		}
	}
	
	///////zeyong.xia  add
	public void setExecuteAt(long delay)
	{
		this.executeAt+=delay;
	}
	
	public long getExecuteAt()
	{
		return this.executeAt;
	}
}
