package com.babeeta.butterfly.application.app.controller;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.babeeta.butterfly.application.app.record.entity.MessageRecord;
import com.babeeta.butterfly.application.app.service.ChangeRecipientService;
/***
 * 变更收件人
 * @author zeyong.xia
 * @date 2011-9-19
 */
@Path(value = "/change")
@Scope(value = "prototype")
@Controller
public class ChangeRecipientResource {

	private ChangeRecipientService changeRecipientServiceImpl;
	
	private final static Logger logger = LoggerFactory
	.getLogger(ChangeRecipientResource.class);
	@PUT
	@Path(value="/recipilent/{oldCid}/{newCid}")
	@Consumes("application/octet-stream")
	@Produces("text/plain;charset=UTF-8")
	public Response changeRecipient(@PathParam("oldCid") String oldCid,
			@PathParam("newCid")String newCid,String aid)
	{
		logger.debug("[ChangeRecipientResource]changeRecipient from {} to {}.", oldCid,newCid);
		//验证 aid and key
		//boolean flag=this.changeRecipientServiceImpl.auth(aid, key);//验证通过
		if(true)
		{
			//String oldDid=this.changeRecipientServiceImpl.queryDid(aid, oldCid);
			//查询某应用终端处于延时推送的消息列表
			System.out.println("***1***"+this.changeRecipientServiceImpl);
			List<MessageRecord> lists=this.changeRecipientServiceImpl.queryNotAppAckMessage(aid, oldCid, "DELAYING");
			//锁定消息状态
			this.changeRecipientServiceImpl.lockMessageStatus(lists, "LOCK");
			//this.changeRecipientServiceImpl.updateRecipient(oldCid, newCid, aid);
			//将SCHEDULED_TASK_QUEUE中即将运行的Task取出并set回去
			//this.changeRecipientServiceImpl.pauseTaskTimer();
			String newDid=this.changeRecipientServiceImpl.queryDid(aid, newCid);
			this.changeRecipientServiceImpl.updateRelationship(aid, oldCid, newDid);
			//List<ReliablePushBean> list=this.changeRecipientServiceImpl.queryNotAppAckMessageId(aid, oldCid);
			//修改可靠投递未成功的目的地
			this.changeRecipientServiceImpl.updateCid(oldCid, newCid, aid);
			this.changeRecipientServiceImpl.lockMessageStatus(lists, "DELAYING");
			
			//开始锁定消息状态，目前未实现
			//TODO//以下不应该重新投递，应该让客户端上线或心跳来触发
//			if(list!=null&&list.size()>0)
//			{
//				//开始重新投递
//				//一下还有问题，不应该立即推送，因为延时的时间还未到
//				//TODO
//				for(ReliablePushBean rp:list)
//				{
//					MessageRouting.Message.Builder builder = MessageRouting.Message
//					.newBuilder()
//					.setDate(System.currentTimeMillis())
//					.setExpire(0)//TODO过期时间设定
//					.setFrom(aid + "@" + "0.gateway.app")
//					.setUid(rp.getParentId());
//
//					MessageRouting.Message msg = null;
//					builder.setContent(ByteString.copyFrom(rp.getMessage()));
//					builder.setTo(new StringBuilder(newCid).append(".")
//							.append(aid)
//							.append("@dev").toString());
//					msg = builder.build();
//					msg = builder.build();
//					ReliablePush reliablePush = ReliablePushImpl
//								.getDefaultInstance();
//					boolean saveResult = reliablePush.saveMessage(msg,
//								aid,
//								newCid);
//					logger.debug("[{}] ReliablePush[{}] [{}]",
//							new Object[] {
//									rp.getParentId(),
//									saveResult,
//									new StringBuilder(aid).append(".")
//											.append(newCid)
//											.toString() });
//				if (!saveResult) {
//					logger.warn("[{}]failed to persistence.", rp.getParentId());
//				}
//				//推送消息
//				 new MessageSenderImpl().send(msg);
//				}
//			}
		}
		return Response.status(200).build();
	}

	public void setChangeRecipientServiceImpl(
			ChangeRecipientService changeRecipientServiceImpl) {
		this.changeRecipientServiceImpl = changeRecipientServiceImpl;
	}
	
	
}
