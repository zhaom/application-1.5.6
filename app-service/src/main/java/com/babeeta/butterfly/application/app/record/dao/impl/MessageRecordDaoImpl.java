package com.babeeta.butterfly.application.app.record.dao.impl;

import java.util.Date;
import java.util.List;
import com.babeeta.butterfly.application.app.MessageContext;
import com.babeeta.butterfly.application.app.record.dao.MessageRecordDao;
import com.babeeta.butterfly.application.app.record.entity.MessageRecord;
import com.babeeta.butterfly.application.app.schedule.ScheduledMessageTask;
import com.babeeta.butterfly.application.app.schedule.ScheduledTaskService;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class MessageRecordDaoImpl extends BasicDaoImpl implements
		MessageRecordDao {
	private final String STATUS_DELETED = "DELETED";
	private final String STATUS_DELAYING = "DELAYING";
	private final String STATUS_DELIVERING = "DELIVERING";// 投递中，未ack
	private final String STATUS_ACKED = "ACKED";// 已act
	private final String STATUS_APP_ACKED = "APP_ACKED";// 已act
	private final String STATUS_EXPIRED = "EXPIRED";// 已过期

	public MessageRecordDaoImpl() {
		datastore = morphia.createDatastore(mongo, "MessageRecord");
		datastore.ensureIndexes();
	}

	@Override
	public boolean saveMessageRecord(MessageContext mtx) {
		MessageRecord record = new MessageRecord();

		record.setMessageId(mtx.getMessageId());
		record.setAppId(mtx.getSender());
		record.setRecipient(mtx.getRecipient());
		record.setDataType(mtx.getDataType());
		record.setContent(mtx.getContent());
		record.setExpire(mtx.getExpire());
		record.setBroadcastFlag(mtx.getBroadcastFlag());

		record.setStatus(STATUS_DELAYING);
		///////zeyong.xia add
		record.setCreateAt(new Date());
		record.setDelay(mtx.getDelay());
		/////////////
		/*
		 * if (mtx.getDelay() > 0) {
		 * 
		 * } else { record.setStatus(STATUS_DELIVERING); }
		 */
		record.setTotalSubMessage(0);
		record.setAckedCount(0);
		record.setAppAckedCount(0);

		record.setLastModified(new Date());

		datastore.save(record);

		return true;
	}

	@Override
	public boolean saveBroadcastMessageRecord(MessageContext mtx,
			int totalSubMessage) {
		MessageRecord record = new MessageRecord();

		record.setMessageId(mtx.getMessageId());
		record.setAppId(mtx.getSender());
		record.setRecipient(mtx.getRecipient());
		record.setDataType(mtx.getDataType());
		record.setContent(mtx.getContent());
		record.setExpire(mtx.getExpire());

		if (mtx.getDelay() > 0) {
			record.setStatus(STATUS_DELAYING);
		} else {
			record.setStatus(STATUS_DELIVERING);
		}
		record.setTotalSubMessage(totalSubMessage);
		record.setAckedCount(0);
		record.setAppAckedCount(0);

		record.setLastModified(new Date());

		datastore.save(record);

		return true;
	}

	@Override
	public boolean removeMessageRecord(String messageId) {
		Query<MessageRecord> query = datastore.createQuery(MessageRecord.class)
				.filter("_id", messageId);
		MessageRecord result = query.get();
		if (result != null) {
			if (result.getStatus().equals(STATUS_DELAYING)) {
				UpdateOperations<MessageRecord> ops = datastore
						.createUpdateOperations(MessageRecord.class)
						.set("status", STATUS_DELETED)
						.set("lastModified", new Date());
				datastore.update(query, ops);
				return true;
			}
		}
		return false;
	}

	@Override
	public String getMessageStatus(String messageId) {
		MessageRecord record = datastore.get(MessageRecord.class, messageId);

		if (record != null) {
			if (record.getTotalSubMessage() > 1) {
				StringBuilder result = new StringBuilder();
				result.append(record.getStatus());
				result.append(" details:[" + record.getAppAckedCount()
							+ ","
							+ record.getAckedCount() + ","
							+ record.getTotalSubMessage() + "]");
				return result.toString();
			} else {
				return record.getStatus();
			}
		} else {
			return null;
		}

	}

	@Override
	public MessageRecord getMessageRecordbyId(String messageId) {
		MessageRecord record = new MessageRecord();
		record.setMessageId(messageId);
		record = datastore.get(record);
		return record;
	}

	@Override
	public boolean updateAppAcked(String messageId) {
		Query<MessageRecord> query = datastore.createQuery(MessageRecord.class)
				.filter("_id", messageId);
		MessageRecord result = query.get();
		if (result != null) {
			if (result.getTotalSubMessage() > 1
					&& result.getAckedCount() > result.getAppAckedCount()) {
				UpdateOperations<MessageRecord> ops = datastore
						.createUpdateOperations(MessageRecord.class)
						.set("lastModified", new Date())
						.set("appAckedCount",
								result.getAppAckedCount() + 1);
				datastore.update(query, ops);
				return true;
			} else {
				if (result.getStatus().equals(STATUS_ACKED)) {
					UpdateOperations<MessageRecord> ops = datastore
							.createUpdateOperations(MessageRecord.class)
							.set("status", STATUS_APP_ACKED)
							.set("lastModified", new Date())
							.set("appAckedCount", 1);
					datastore.update(query, ops);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean updateAcked(String messageId) {
		Query<MessageRecord> query = datastore.createQuery(MessageRecord.class)
				.filter("_id", messageId);
		MessageRecord result = query.get();
		if (result != null) {
			if (result.getTotalSubMessage() > 1
					&& result.getAckedCount() < result.getTotalSubMessage()) {
				UpdateOperations<MessageRecord> ops = datastore
						.createUpdateOperations(MessageRecord.class)
						.set("lastModified", new Date())
						.set("ackedCount", result.getAckedCount() + 1);
				datastore.update(query, ops);
				return true;
			} else {
				if (result.getStatus().equals(STATUS_DELIVERING)) {
					UpdateOperations<MessageRecord> ops = datastore
							.createUpdateOperations(MessageRecord.class)
							.set("status", STATUS_ACKED)
							.set("lastModified", new Date())
							.set("ackedCount", 1);
					datastore.update(query, ops);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean updateExpired(String messageId) {
		Query<MessageRecord> query = datastore.createQuery(MessageRecord.class)
				.filter("_id", messageId);
		MessageRecord result = query.get();
		if (result != null) {
			if (result.getStatus().equals(STATUS_DELIVERING)) {
				UpdateOperations<MessageRecord> ops = datastore
						.createUpdateOperations(MessageRecord.class)
						.set("status", STATUS_EXPIRED)
						.set("lastModified", new Date());
				datastore.update(query, ops);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean updateDelivering(String messageId, int receiver) {
		Query<MessageRecord> query = datastore.createQuery(MessageRecord.class)
				.filter("_id", messageId);
		MessageRecord result = query.get();
		if (result != null) {
			if (result.getStatus().equals(STATUS_DELAYING)) {
				UpdateOperations<MessageRecord> ops = datastore
						.createUpdateOperations(MessageRecord.class)
						.set("status", STATUS_DELIVERING)
						.set("totalSubMessage", receiver)
						.set("lastModified", new Date());
				datastore.update(query, ops);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean modifyMessageRecordContent(String messageId,
			String dataType, byte[] content) {
		Query<MessageRecord> query = datastore.createQuery(MessageRecord.class)
				.filter("_id", messageId);
		MessageRecord result = query.get();
		if (result != null) {
			if (result.getStatus().equals(STATUS_DELAYING)) {
				UpdateOperations<MessageRecord> ops = datastore
						.createUpdateOperations(MessageRecord.class)
						.set("dataType", dataType)
						.set("content", content)
						.set("lastModified", new Date());
				datastore.update(query, ops);
				return true;
			}
		}
		return false;
	}
	
//以下接口为新增接口
	
	/***
	 * 查询未app ack的消息列表
	 */
	public List<MessageRecord> queryNotAppAckMessage(String aid,String cid,String staus)
	{
		Query<MessageRecord> query = datastore.createQuery(MessageRecord.class).filter("appId", aid)
		.filter("recipient", cid).filter("status", staus);
		return query.asList();
	}
	
	
	/***
	 * 锁定消息状态
	 * @param list
	 */
	public void lockMessageStatus(List<MessageRecord> list,String status)
	{
		if(list!=null&&list.size()>0)
		{
			
			for(MessageRecord msg:list)
			{
				Query<MessageRecord> query=this.datastore.createQuery(MessageRecord.class)
				                           .filter("_id", msg.getMessageId());
				UpdateOperations<MessageRecord> ops = datastore
				.createUpdateOperations(MessageRecord.class);
				ops.set("status", status);
				this.datastore.update(query, ops);
			}
		}
	}
	
	/***
	 * 修改收件人
	 * @param oldCid
	 * @param newCid
	 */
	public void updateRecipient(String oldCid,String newCid,String aid)
	{
		Query<MessageRecord> querys = datastore.createQuery(MessageRecord.class).filter("appId", aid)
		.filter("recipient", oldCid).filter("status", "LOCK");
		List<MessageRecord> list=querys.asList();
		if(list!=null&&list.size()>0)
		{
			for(MessageRecord msg:list)
			{
				Query<MessageRecord> query=this.datastore.createQuery(MessageRecord.class)
				                           .filter("_id", msg.getMessageId());
				UpdateOperations<MessageRecord> ops = datastore
				.createUpdateOperations(MessageRecord.class);
				ops.set("recipient", newCid);
				this.datastore.update(query, ops);
				if(new Date().getTime()-(msg.getCreateAt().getTime()+msg.getDelay()*60*1000)<60*1000)
				{
					ScheduledMessageTask task=new ScheduledMessageTask(msg.getMessageId(),msg.getDelay()+1);
					ScheduledTaskService.getDefaultInstance().setupTask(task);
				}
				else
				{
					ScheduledMessageTask task=new ScheduledMessageTask(msg.getMessageId(),msg.getDelay());
					ScheduledTaskService.getDefaultInstance().setupTask(task);
				}
			}
		}
	}
}
