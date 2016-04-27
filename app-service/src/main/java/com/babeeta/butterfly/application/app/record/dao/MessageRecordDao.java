package com.babeeta.butterfly.application.app.record.dao;

import java.util.List;

import com.babeeta.butterfly.application.app.MessageContext;
import com.babeeta.butterfly.application.app.record.entity.MessageRecord;

public interface MessageRecordDao {
	public boolean saveMessageRecord(MessageContext mtx);

	public boolean saveBroadcastMessageRecord(MessageContext mtx,
			int totalSubMessage);

	public String getMessageStatus(String messageId);

	public boolean updateAppAcked(String messageId);

	public boolean updateAcked(String messageId);

	public boolean updateExpired(String messageId);

	public boolean updateDelivering(String messageId, int receiver);

	public boolean removeMessageRecord(String messageId);

	public boolean modifyMessageRecordContent(String messageId,
			String dataType,
			byte[] content);

	public MessageRecord getMessageRecordbyId(String messageId);
	
	//以下接口为新增接口
	
	/***
	 * 查询未app ack的消息列表
	 */
	public List<MessageRecord> queryNotAppAckMessage(String aid,String cid,String status);
	
	/***
	 * 锁定消息状态
	 * @param list
	 */
	public void lockMessageStatus(List<MessageRecord> list,String status);
	
	/***
	 * 修改收件人
	 * @param oldCid
	 * @param newCid
	 */
	public void updateRecipient(String oldCid,String newCid,String aid);
	
}
