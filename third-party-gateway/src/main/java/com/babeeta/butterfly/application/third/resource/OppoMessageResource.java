package com.babeeta.butterfly.application.third.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import com.babeeta.butterfly.application.third.service.app.AppServiceResult;
import com.babeeta.butterfly.application.third.service.app.OppoAppServiceImpl;
import com.babeeta.butterfly.application.third.service.words.FilterWordsService;

/***
 * 为oppo定制的服务
 * @author zeyong.xia
 * @date 2011-9-23
 */
@Controller
@Path("/1/api/message")
@Scope(value = "prototype")
public class OppoMessageResource {
	private final static Logger logger = LoggerFactory
	.getLogger(OppoMessageResource.class);
	
	private MessageConfig userMessageConfig;
	
	private MessageConfig groupMessageConfig;	
	
	private OppoAppServiceImpl oppoAppServiceImpl;
	
	private FilterWordsService filterWordsServiceImpl;

	private String[] getAuthContent(String authorization) {
		try {
			String base64Content = authorization.split(" ")[1];
			String authContent = new String(Base64.decodeBase64(base64Content),
					"UTF-8");
			return authContent.split(":");
		} catch (Exception e) {
			logger.error("[authorization header] {}", e.getMessage());
			return null;
		}
	}
	
	/***
	 * 广播组播
	 * @return
	 */
	@POST
	@Path("/broadcast/{tagExpression}/{messageInfo}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_PLAIN)
	public Response pushToBroadcast(@PathParam("tagExpression") String tagExpression,
			@PathParam("messageInfo") PathSegment messageInfo,
			@HeaderParam("authorization") String authorization,
			byte[] messageContent)
	{
		logger.debug("[OppoMessageResource] pushToBroadcast to {}",tagExpression);
		byte[] content;
		int lifeValue = groupMessageConfig.getLifeValueDefault();
		int delayValue = groupMessageConfig.getDelayValueDefault();
		MessageContentType contentType = null;

		String life = messageInfo.getMatrixParameters().getFirst("life");
		String delay = messageInfo.getMatrixParameters().getFirst("delay");
		String type = messageInfo.getMatrixParameters().getFirst("type");

		if (type == null) {
			logger.debug("[OppoMessageResource] pushToBroadcast to {}, type is null",tagExpression);
			return Response.status(406).build();
		}

		contentType = MessageContentType.getByTag(type);

		if (contentType == null) {
			logger.debug("pushToBroadcast to {}, contentType is null",tagExpression);
			return Response.status(406).build();
		}

		if (messageContent.length == 0) {
			logger.debug("pushToBroadcast to {}, messageContent.length == 0",tagExpression);
			return Response.status(422).build();
		}

		if (messageContent.length > groupMessageConfig.getLengthValueMax()) {
			return Response.status(413).build();
		}

		if (life != null) {
			lifeValue = groupMessageConfig.verifyLifeValue(Integer
					.valueOf(life));
		}

		if (delay != null) {
			delayValue = groupMessageConfig.verifyDelayValue(Integer
					.valueOf(delay));
			System.out.println("delay= "+delayValue);
			//xaizeyong add filter message
			if(contentType.equals(MessageContentType.TYPE_TEXT))
			{
//				AppServiceResult res=filterWordsServiceImpl.filterMessage(messageContent.toString());
//				if(!res.isSuccess())
//				{
//					return Response.status(res.getStatusCode()).build();
//				}
			}
		}
      
		MessageType messageType = contentType
				.equals(MessageContentType.TYPE_BINARY) ? MessageType.BINARY
				: MessageType.JSON;

		if (messageType.equals(MessageType.JSON)) {
			String json = "{\"type\":\"" + contentType.getTag()
					+ "\",\"body\":\"" + messageContent.toString() + "\"}";
			content = json.getBytes();
		} else {
			content = messageContent;
		}

		MessageContext mtx = new MessageContext(messageType,
				content);
		mtx.setLife(lifeValue);
		mtx.setDelay(delayValue);
		String appId = getAuthContent(authorization)[0];
		AppServiceResult result = oppoAppServiceImpl.pushBroadcastMessage(mtx, appId,
				tagExpression);

		if (result.isSuccess()) {
			return Response.status(200).entity(result.getMessageId()).build();
		} else {
			logger.debug("[OppoMessageResource] pushToBroadcast  fail");
			return Response.status(result.getStatusCode()).build();
		}
	}
	
	@POST
	@Path("/client/{clientId}/{messageInfo}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_PLAIN)
	public Response pushToClient(
			@PathParam("clientId") String clientId,
			@PathParam("messageInfo") PathSegment messageInfo,
			@HeaderParam("authorization") String authorization,
			byte[] messageContent)
	{
		logger.debug("[OppoMessageResource] pushToClient to {}",clientId);
		int lifeValue = userMessageConfig.getLifeValueDefault();
		int delayValue = userMessageConfig.getDelayValueDefault();
		MessageContentType contentType = null;

		String life = messageInfo.getMatrixParameters().getFirst("life");
		String delay = messageInfo.getMatrixParameters().getFirst("delay");
		String type = messageInfo.getMatrixParameters().getFirst("type");

		if (type == null) {
			return Response.status(406).build();
		}

		contentType = MessageContentType.getByTag(type);

		if (contentType == null) {
			return Response.status(406).build();
		}

		if (messageContent.length == 0) {
			return Response.status(422).build();
		}

		if (messageContent.length > userMessageConfig.getLengthValueMax()) {
			return Response.status(413).build();
		}

		if (life != null) {
			lifeValue = userMessageConfig.verifyLifeValue(Integer
					.valueOf(life));
		}

		if (delay != null) {
			delayValue = userMessageConfig.verifyDelayValue(Integer
					.valueOf(delay));
			//xaizeyong add filter message
			if(contentType.equals(MessageContentType.TYPE_TEXT))
			{
				//过滤敏感词
//				AppServiceResult res=filterWordsServiceImpl.filterMessage(messageContent.toString());
//				if(!res.isSuccess())
//				{
//					return Response.status(res.getStatusCode()).build();
//				}
			}
		}
		MessageType messageType = contentType
				.equals(MessageContentType.TYPE_BINARY) ? MessageType.BINARY
				: MessageType.JSON;

		MessageContext mtx = new MessageContext(messageType,
					messageContent);

		mtx.setLife(lifeValue);
		mtx.setDelay(delayValue);
		String appId = getAuthContent(authorization)[0];
		AppServiceResult result = oppoAppServiceImpl.pushMessage(mtx, appId, clientId);

		if (result.isSuccess()) {
			return Response.status(200).entity(result.getMessageId()).build();
		} else {
			logger.debug("[OppoMessageResource] pushToClient fail");
			return Response.status(result.getStatusCode()).build();
		}
	
	}
	/***
	 * 变更目的地
	 * @return
	 */
	@PUT
	@Path("/client/change/{oldCid}/{newCid}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_PLAIN)
	public Response changeRecipient(@PathParam("oldCid")String oldCid,@PathParam("newCid")String newCid,@HeaderParam("authorization")String authorization)
	{
		logger.debug("[OppoMessageResource] changeRecipient from {} to {}",oldCid,newCid);
	  if(oldCid==null||oldCid.equals("")||oldCid=="")
	  {
		  return Response.status(406).build();
	  }
	  if(newCid==null||newCid.equals("")||newCid=="")
	  {
		  return Response.status(406).build();
	  }
	  String aid=getAuthContent(authorization)[0];
	  if(aid==null||aid.equals("")||aid=="")
	  {
		  return Response.status(406).build();
	  }
	  AppServiceResult result =this.oppoAppServiceImpl.changeRecipient(oldCid, newCid, aid);
	  if (result.isSuccess()) {
			return Response.status(200).entity(result.getMessageId()).build();
		} else {
			logger.debug("[OppoMessageResource] changeRecipient fail");
			return Response.status(result.getStatusCode()).build();
		}
	}
	
	/***\
	 * 修改消息体
	 * @return
	 */
	@PUT
	@Path("/update/{messageId}/type={type}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public Response modifyMessageContext(
			@PathParam("messageId") String messageId,
			@PathParam("type") String type,
			byte[] messageContent)
	{
		logger.debug("[OppoMessageResource] modifyMessageContext ,messageId is {}",messageId);
		MessageContentType contentType = null;
		if (messageId == null) {
			return Response.status(404).build();
		}
		if (type == null) {
			return Response.status(406).build();
		}
		contentType = MessageContentType.getByTag(type);

		if (contentType == null) {
			return Response.status(406).build();
		}

		AppServiceResult result = oppoAppServiceImpl.modifyMessageContent(messageId,
				contentType.getTag(), messageContent);

		if (result.isSuccess()) {
			return Response.status(200).build();
		} else {
			logger.debug("[OppoMessageResource] modifyMessageContext fail");
			return Response.status(result.getStatusCode()).build();
		}
	
	}
	
	/***
	 * 删除消息
	 * @return
	 */
	@DELETE
	@Path("/delete/{messageId}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteMessage(@PathParam("messageId") String messageId)
	{
		logger.debug("[OppoMessageResource] deleteMessage ,messageId is {}",messageId);
		if (messageId == null) {
			return Response.status(404).build();
		}
		AppServiceResult result = oppoAppServiceImpl.deleteMessage(messageId);

		if (result.isSuccess()) {
			return Response.status(200).build();
		} else {
			logger.debug("[OppoMessageResource] deleteMessage fail");
			return Response.status(result.getStatusCode()).build();
		}
	}
	
	/***
	 * 查询消息状态
	 * @return
	 */
	@GET
	@Path("/query/{messageId}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response queryMessageStatus(@PathParam("messageId") String messageId)
	{
		logger.debug("[OppoMessageResource] queryMessageStatus ,messageId is {}",messageId);
		if (messageId == null) {
			return Response.status(404).build();
		}
		AppServiceResult result = oppoAppServiceImpl.queryMessageStatus(messageId);

		if (result.isSuccess()) {
			return Response.status(200).entity(result.getMessageStatus())
					.build();
		} else {
			logger.debug("[OppoMessageResource] queryMessageStatus fail");
			return Response.status(result.getStatusCode()).build();
		}
	}

	public void setOppoAppServiceImpl(OppoAppServiceImpl oppoAppServiceImpl) {
		this.oppoAppServiceImpl = oppoAppServiceImpl;
	}

	public void setFilterWordsServiceImpl(FilterWordsService filterWordsServiceImpl) {
		this.filterWordsServiceImpl = filterWordsServiceImpl;
	}

	public void setUserMessageConfig(MessageConfig userMessageConfig) {
		this.userMessageConfig = userMessageConfig;
	}

	public void setGroupMessageConfig(MessageConfig groupMessageConfig) {
		this.groupMessageConfig = groupMessageConfig;
	}	
	
	
}
