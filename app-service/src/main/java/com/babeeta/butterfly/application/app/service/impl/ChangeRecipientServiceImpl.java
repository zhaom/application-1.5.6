package com.babeeta.butterfly.application.app.service.impl;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.babeeta.butterfly.application.app.record.dao.MessageRecordDao;
import com.babeeta.butterfly.application.app.record.entity.MessageRecord;
import com.babeeta.butterfly.application.app.reliablepush.dao.impl.ReliablePushDaoImpl;
import com.babeeta.butterfly.application.app.reliablepush.entity.ReliablePushBean;
import com.babeeta.butterfly.application.app.schedule.ScheduledMessageTask;
import com.babeeta.butterfly.application.app.schedule.ScheduledTaskService;
import com.babeeta.butterfly.application.app.service.ChangeRecipientService;
import com.babeeta.butterfly.application.app.subscription.dao.SubscriptionDao;

@SuppressWarnings("deprecation")
public class ChangeRecipientServiceImpl implements ChangeRecipientService {

	private SubscriptionDao subscriptionDaoImpl;
	
	private ReliablePushDaoImpl reliablePushDaoImpl;
	
	private MessageRecordDao messageRecordDaoImpl;
	
	/***
	 * 验证
	 * @param aid
	 * @param key
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean auth(String aid,String key)
	{
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 100);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
					new Scheme("http", PlainSocketFactory.getSocketFactory(),
							80));
		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);
		HttpClient client = new DefaultHttpClient(cm, params);
		String json = "{\"id\":\"" + aid + "\",\"secureKey\":\"" + key
				+ "\"}";
		HttpPost httpPost = new HttpPost("http://accounts.app/api/auth");
		httpPost.setEntity(new ByteArrayEntity(json.getBytes()));
		httpPost.setHeader(new BasicHeader("Content-type", "application/json"));
		try
		{
			HttpResponse httpResponse = client.execute(httpPost);
			if(HttpStatus.SC_OK == httpResponse.getStatusLine()
			.getStatusCode())
			{
				return true;
			}
			return false;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	/***
	 * 更新aid,cid与did的关系
	 * @param aid
	 * @param oldCid
	 * @param newCid
	 * @return
	 */
	public boolean updateRelationship(String aid,String oldCid,String newDid)
	{
		return this.subscriptionDaoImpl.updateRelationship(aid, oldCid, newDid);
	}
	
	/***
	 * 重置消息状态
	 * @param messageId
	 */
	public void resetMessageAck(String messageId)
	{
		//TODO
	}

	/***
	 * 查询未ack的消息ID
	 * @param aid
	 * @param cid
	 * @return
	 */
	public List<ReliablePushBean> queryNotAppAckMessageId(String aid,String cid)
	{
		return this.reliablePushDaoImpl.queryNotAppAckMessageId(aid, cid);
	}
	
	/***
	 * 查询did
	 * @param aid
	 * @param cid
	 * @return
	 */
	public String queryDid(String aid,String cid)
	{
		return this.subscriptionDaoImpl.querydid(aid, cid);
	}
	
	/***
	 * 查询未app ack的消息列表
	 */
	public List<MessageRecord> queryNotAppAckMessage(String aid,String cid,String status)
	{
		return this.messageRecordDaoImpl.queryNotAppAckMessage(aid, cid,status);
	}
	
	/***
	 * 锁定消息状态
	 * @param list
	 */
	public void lockMessageStatus(List<MessageRecord> list,String status)
	{
		this.messageRecordDaoImpl.lockMessageStatus(list, status);
	}

	
	
	/***
	 * 暂停 Timer
	 */
	public void pauseTaskTimer()
	{
		ScheduledMessageTask task = null;
		do
		{
			task = ScheduledTaskService
					.getDefaultInstance().getTimeoutTask();
			if (task != null)
			{
				//延时5秒
				task.setExecuteAt(5*1000);
				ScheduledTaskService.getDefaultInstance().setupTask(task);
			}
		} while (task != null);

	}
	
	/***
	 * 修改收件人
	 * @param oldCid
	 * @param newCid
	 * @param aid
	 */
	public void updateRecipient(String oldCid,String newCid,String aid)
	{
		this.messageRecordDaoImpl.updateRecipient(oldCid, newCid, aid);
	}
	
	/***
	 * 修改收件人
	 * @param oldCid
	 * @param newCid
	 * @param aid
	 */
	public void updateCid(String oldCid,String newCid,String aid)
	{
		this.reliablePushDaoImpl.updateCid(oldCid, newCid, aid);
	}
	
	public void setSubscriptionDaoImpl(SubscriptionDao subscriptionDaoImpl) {
		this.subscriptionDaoImpl = subscriptionDaoImpl;
	}

	public void setReliablePushDaoImpl(ReliablePushDaoImpl reliablePushDaoImpl) {
		this.reliablePushDaoImpl = reliablePushDaoImpl;
	}

	public void setMessageRecordDaoImpl(MessageRecordDao messageRecordDaoImpl) {
		this.messageRecordDaoImpl = messageRecordDaoImpl;
	}
	
	
}
