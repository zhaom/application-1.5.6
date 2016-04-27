package com.babeeta.butterfly.application.app.controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.babeeta.butterfly.application.app.MessageContext;
import com.babeeta.butterfly.application.app.pusher.MessagePusher;
import com.babeeta.butterfly.application.app.record.MessageRecordService;
import com.babeeta.butterfly.application.app.record.entity.MessageRecord;
import com.babeeta.butterfly.application.app.schedule.ScheduledMessageTask;
import com.babeeta.butterfly.application.app.schedule.ScheduledTaskService;

@Path("/")
@Scope(value = "prototype")
@Component("appResource")
public class AppResource {
	private final static Logger logger = LoggerFactory
			.getLogger(AppResource.class);

	private final int MSG_CONTENT_MAX_LENGTH = 8192;

	@POST
	@Path("/message/push/group/{sender}/{recipient}")
	@Consumes("application/octet-stream")
	@Produces("text/plain;charset=UTF-8")
	public Response pushGroupMessage(
			@HeaderParam("DataType") String dataType,
			@HeaderParam("exptime") int exptime,
			@HeaderParam("delay") int delay,
			@PathParam("sender") String sender,
			@PathParam("recipient") String recipient,
			byte[] content) {
		logger.debug("[AppResource]pushMessage to {}.", recipient);
		if (recipient == null || sender == null) {
			return Response.status(404).build();
		}
		if (content == null || content.length == 0) {
			return Response.status(422).build();
		}

		if (content.length > MSG_CONTENT_MAX_LENGTH) {
			return Response.status(413).build();
		}

		String strUUID = java.util.UUID.randomUUID().toString()
				.replaceAll("-", "");
		MessageContext mtx = new MessageContext();
		mtx.setMessageId(strUUID);
		mtx.setParentId(strUUID);
		mtx.setSender(sender);
		mtx.setRecipient(recipient);
		mtx.setDataType(dataType);
		mtx.setExpire(exptime);
		mtx.setDelay(delay);
		mtx.setContent(content);
		mtx.setBroadcastFlag(true);
		
		//////////以下由zeyong.xia修改
		if(delay==0)
		{
			mtx.setStatus("DELIVERING");
		}
		else
		{
			mtx.setStatus("DELAYING");
		}
		//////////////////////
		
		
		if (MessageRecordService.getDefaultInstance().getDao()
				.saveMessageRecord(mtx)) {
			if (delay == 0) {
				logger.debug("Push message [{}] immediately.", strUUID);
				MessagePusher.getDefaultInstance().broadcast(mtx);
			}
			////////zeyong.xia  add 
			//延时推送
			else
			{
				ScheduledMessageTask task=new ScheduledMessageTask(mtx.getMessageId(),delay);
				ScheduledTaskService.getDefaultInstance().setupTask(task);
			}
			return Response.status(200).entity(strUUID).build();
		} else {
			logger.error("save message {} failed.", strUUID);
			return Response.status(500).build();
		}
	}

	@POST
	@Path("/message/push/single/{sender}/{recipient}")
	@Consumes("application/octet-stream")
	@Produces("text/plain;charset=UTF-8")
	public Response pushSingleMessage(
			@HeaderParam("DataType") String dataType,
			@HeaderParam("exptime") int exptime,
			@HeaderParam("delay") int delay,
			@PathParam("sender") String sender,
			@PathParam("recipient") String recipient,
			byte[] content) {
		logger.debug("[AppResource] pushSingleMessage to {}.", recipient);
		if (recipient == null || sender == null) {
			return Response.status(404).build();
		}

		if (content == null || content.length == 0) {
			return Response.status(422).build();
		}

		if (content.length > MSG_CONTENT_MAX_LENGTH) {
			return Response.status(413).build();
		}

		String strUUID = java.util.UUID.randomUUID().toString()
				.replaceAll("-", "");
		MessageContext mtx = new MessageContext();
		mtx.setMessageId(strUUID);
		mtx.setParentId(strUUID);
		mtx.setSender(sender);
		mtx.setRecipient(recipient);
		mtx.setDataType(dataType);
		mtx.setExpire(exptime);
		mtx.setDelay(delay);
		mtx.setContent(content);
		mtx.setBroadcastFlag(false);

		//////////以下由zeyong.xia修改
		if(delay==0)
		{
			mtx.setStatus("DELIVERING");
		}
		else
		{
			mtx.setStatus("DELAYING");
		}
		//////////////////////
		
		
		if (MessageRecordService.getDefaultInstance().getDao()
				.saveMessageRecord(mtx)) {
			if (delay == 0) {
				logger.debug("Push message [{}] immediately.", strUUID);
				MessagePusher.getDefaultInstance().unicast(recipient,mtx);
			}
			////////zeyong.xia  add 
			//延时推送
			else
			{
				ScheduledMessageTask task=new ScheduledMessageTask(mtx.getMessageId(),delay);
				ScheduledTaskService.getDefaultInstance().setupTask(task);
			}
			return Response.status(200).entity(strUUID).build();
		} else {
			logger.error("save message {} failed.", strUUID);
			return Response.status(500).build();
		}
	}

	@GET
	@Path("/message/query/{messageId}")
	@Produces("text/plain;charset=UTF-8")
	public Response queryMessageStatus(
			@PathParam("messageId") String messageId) {
		logger.debug("[AppResource]queryMessageStatus of {}.", messageId);
		if (messageId == null) {
			return Response.status(404).build();
		}
		String status = MessageRecordService.getDefaultInstance()
				.getDao().getMessageStatus(messageId);
		if (status != null) {
			return Response.status(200)
					.entity(status).build();
		} else {
			return Response.status(500).build();
		}
	}

	@PUT
	@Path("/message/update/{messageId}")
	@Consumes("application/octet-stream")
	@Produces("text/plain;charset=UTF-8")
	public Response updateMessage(
			@HeaderParam("DataType") String dataType,
			@PathParam("messageId") String messageId,
			byte[] content) {
		logger.debug("[AppResource]updateMessage: {}.", messageId);
		if (messageId == null) {
			return Response.status(404).build();
		}
		MessageRecord messageRecord = MessageRecordService.getDefaultInstance()
				.getDao().getMessageRecordbyId(messageId);

		if (messageRecord != null) {
			if (messageRecord.getStatus().equals("DELAYING")) {
				if (MessageRecordService
						.getDefaultInstance()
						.getDao()
						.modifyMessageRecordContent(messageId, dataType,
								content)) {
					return Response.status(200).build();
				} else {
					logger.error("modify message {} content failed.", messageId);
					return Response.status(500).build();
				}
			} else {
				logger.info("Update message {} is in status {}.", messageId,
						messageRecord.getStatus());
				return Response.status(409).build();
			}
		}
		return Response.status(404).build();
	}

	@DELETE
	@Path("/message/delete/{messageId}")
	@Produces("text/plain;charset=UTF-8")
	public Response deleteMessage(
			@PathParam("messageId") String messageId) {
		logger.debug("[AppResource]deleteMessage: {}.", messageId);
		if (messageId == null) {
			return Response.status(404).build();
		}

		MessageRecord messageRecord = MessageRecordService.getDefaultInstance()
				.getDao().getMessageRecordbyId(messageId);

		if (messageRecord != null) {
			if (messageRecord.getStatus().equals("DELAYING")) {
				if (MessageRecordService.getDefaultInstance()
						.getDao().removeMessageRecord(messageId)) {
					return Response.status(200).build();
				} else {
					logger.error("delete Message {} failed.", messageId);
					return Response.status(500).build();
				}
			} else {
				logger.info("Delete message {} is in status {}.", messageId,
						messageRecord.getStatus());
				return Response.status(409).build();
			}
		}
		return Response.status(404).build();
	}
}
