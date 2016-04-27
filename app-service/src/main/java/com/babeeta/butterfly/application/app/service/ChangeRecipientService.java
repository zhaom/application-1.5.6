package com.babeeta.butterfly.application.app.service;

import java.util.List;

import com.babeeta.butterfly.application.app.record.entity.MessageRecord;
import com.babeeta.butterfly.application.app.reliablepush.entity.ReliablePushBean;

/***
 * 变更收件人
 * @author zeyong.xia
 * @date 2011-9-19
 */
public interface ChangeRecipientService {

	/***
	 * 验证
	 * @param aid
	 * @param key
	 * @return
	 */
	public boolean auth(String aid,String key);
	
	/***
	 * 更新aid,cid与did的关系
	 * @param aid
	 * @param oldCid
	 * @param newCid
	 * @return
	 */
	public boolean updateRelationship(String aid,String oldCid,String newDid);
	
	/***
	 * 重置消息状态
	 * @param messageId
	 */
	public void resetMessageAck(String messageId);

	/***
	 * 查询未ack的消息ID
	 * @param aid
	 * @param cid
	 * @return
	 */
	public List<ReliablePushBean> queryNotAppAckMessageId(String aid,String cid);
	
	/***
	 * 查询did
	 * @param aid
	 * @param cid
	 * @return
	 */
	public String queryDid(String aid,String cid);
	
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
	 * 暂停 Timer
	 */
	public void pauseTaskTimer();
	
	/***
	 * 修改收件人
	 * @param oldCid
	 * @param newCid
	 * @param aid
	 */
	public void updateRecipient(String oldCid,String newCid,String aid);
	
	/***
	 * 修改收件人
	 * @param oldCid
	 * @param newCid
	 * @param aid
	 */
	public void updateCid(String oldCid,String newCid,String aid);
	
}
