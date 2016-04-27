package com.babeeta.butterfly.application.reliable;

import java.util.List;

import com.babeeta.butterfly.MessageRouting;

/**
 * Created by IntelliJ IDEA. User: XYuser Date: 11-1-22 Time: 下午1:58 To change
 * this template use File | Settings | File Templates.
 */
public interface ReliablePush {

	/**
	 * 根据设备ID获取未成功投递的消息集合
	 * 
	 * @param did
	 *            设备ID
	 * @return 未成功投递的消息集合
	 */
	List<MessageRouting.Message> getMessagesList(String did);

	/**
	 * 根据消息ID获取消息的投递状态
	 * 
	 * @param uid
	 *            消息ID
	 * @return 消息状态
	 */
	String getMessageStatus(String uid);

	/**
	 * 存储push消息
	 * 
	 * @param uid
	 *            消息的ID
	 * @param aid
	 *            applicationID
	 * @param cid
	 *            clientID
	 * @param message
	 *            消息内容
	 * @return 是否成功 true=成功，false=失败
	 */
	boolean saveMessage(MessageRouting.Message message, String aid, String cid);

	/**
	 * 根据消息ID更改消息的状态
	 * 
	 * @param uid
	 *            消息的ID
	 * @return 是否成功 true=成功，false=失败
	 */
	boolean updateAck(String uid);

	/**
	 * 更改消息状态至过期（expired）
	 * 
	 * @param uid
	 *            消息的ID
	 * @return
	 */
	void updateExpire(String uid);
}
