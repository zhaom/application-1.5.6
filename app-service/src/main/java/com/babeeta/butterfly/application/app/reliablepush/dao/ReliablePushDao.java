package com.babeeta.butterfly.application.app.reliablepush.dao;

import java.util.List;

import com.babeeta.butterfly.application.app.reliablepush.entity.ReliablePushBean;

/***
 * 
 * @author zeyong.xia
 * @date 2011-9-19
 */
public interface ReliablePushDao {

	/***
	 * 查询未ack的消息id
	 * @param aid
	 * @param cid
	 * @return
	 */
	public List<ReliablePushBean> queryNotAppAckMessageId(String aid,String cid);
	
	/***
	 * 锁定消息状态
	 */
	public void updateStatus(List<ReliablePushBean> list);
	
	/***
	 * 重新设置消息状态
	 */
	public void updateStatusToPush(List<ReliablePushBean> list);
	
	/***
	 * 修改cid
	 * @param oldCid
	 * @param newCid
	 * @param aid
	 */
	public void updateCid(String oldCid,String newCid,String aid);
	
}
