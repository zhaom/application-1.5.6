package com.babeeta.butterfly.application.third.service.app;

import com.babeeta.butterfly.application.third.resource.MessageContext;

/***
 * oppo定制服务
 * @author zeyong.xia
 * @date 2011-9-23
 */
public interface OppoAppService {
	
	/***
	 * 推送单条消息
	 * @param message
	 * @param sender
	 * @param recipient
	 * @return
	 */
	public AppServiceResult pushMessage(
			MessageContext message,
			String sender,
			String recipient);

	/***
	 * 广播组播
	 * @param message
	 * @param sender
	 * @param recipient
	 * @return
	 */
	public AppServiceResult pushBroadcastMessage(
			MessageContext message,
			String sender,
			String recipient);
	/***
	 * 查询消息状态
	 * @param messageId
	 * @return
	 */
	public AppServiceResult queryMessageStatus(String messageId);
	
	/***
	 * 删除消息
	 * @param messageId
	 * @return
	 */
	public AppServiceResult deleteMessage(String messageId);

	/****
	 * 修改消息
	 * @param messageId
	 * @param dataType
	 * @param content
	 * @return
	 */
	public AppServiceResult modifyMessageContent(String messageId,
			String dataType, byte[] content);
	
	/****
	 * 更改目的地
	 * @param oldCid
	 * @param newCid
	 * @param aid
	 * @return
	 */
	public AppServiceResult changeRecipient(String oldCid,String newCid,String aid);
	
	}