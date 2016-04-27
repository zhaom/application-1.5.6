package com.babeeta.butterfly.application.third.service.app;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.third.resource.MessageContext;
import com.babeeta.butterfly.application.third.service.AbstractHttpRPCService;

public class OppoAppServiceImpl extends AbstractHttpRPCService implements OppoAppService {

	private static final Logger logger = LoggerFactory
	.getLogger(OppoAppServiceImpl.class);
	
	private static final String APP_SERVICE_HOST = "app.";
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
			String recipient)
	{
		HttpPost httpPost = new HttpPost("/message/push/single/"
				+ sender + "/" + recipient);
		httpPost.addHeader("delay", "" + message.getDelay());
		httpPost.addHeader("exptime", "" + message.getLife());
		httpPost.addHeader("DataType", message.getContentType().toString());
		httpPost.setHeader("Content-type", "application/octet-stream");
		ByteArrayEntity byteArray = new ByteArrayEntity(message.getContent());
		httpPost.setEntity(byteArray);

		HttpResponse httpResponse = null;
		try {
			httpResponse = invoke(httpPost);
			String responseString = EntityUtils.toString(httpResponse
					.getEntity());
			logger.debug("response: {}", responseString);
			if (httpResponse.getStatusLine().getStatusCode() == 404) {
				return new AppServiceResult(false, 404);
			} else if (httpResponse.getStatusLine().getStatusCode() == 200) {
				AppServiceResult result = new AppServiceResult(true, 200);
				result.setMessageId(responseString);
				return result;
			} else {
				return new AppServiceResult(false, 500);
			}

		} catch (Exception e) {
			LOGGER.error("[pushMessage] "
					+ httpPost.getURI().getPath() + " failed.", e);
			return new AppServiceResult(false, 500);
		}
	}

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
			String recipient)
	{
		HttpPost httpPost = new HttpPost("/message/push/group/"
				+ sender + "/" + recipient);
		httpPost.addHeader("delay", "" + message.getDelay());
		httpPost.addHeader("exptime", "" + message.getLife());
		httpPost.addHeader("DataType", message.getContentType().toString());
		httpPost.setHeader("Content-type", "application/octet-stream");
		ByteArrayEntity byteArray = new ByteArrayEntity(message.getContent());
		httpPost.setEntity(byteArray);

		HttpResponse httpResponse = null;
		try {
			httpResponse = invoke(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 404) {
				return new AppServiceResult(false, 404);
			} else if (httpResponse.getStatusLine().getStatusCode() == 200) {
				AppServiceResult result = new AppServiceResult(true, 200);
				result.setMessageId(EntityUtils
						.toString(httpResponse.getEntity()));
				return result;
			} else {
				return new AppServiceResult(false, 500);
			}

		} catch (Exception e) {
			LOGGER.error("[pushBroadcastMessage] "
					+ httpPost.getURI().getPath() + " failed.", e);
			return new AppServiceResult(false, 500);
		}
	}
	/***
	 * 查询消息状态
	 * @param messageId
	 * @return
	 */
	public AppServiceResult queryMessageStatus(String messageId)
	{
		HttpGet httpGet = new HttpGet("/message/query/"
				+ messageId);

		HttpResponse httpResponse = null;
		try {
			httpResponse = invoke(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == 404) {
				return new AppServiceResult(false, 404);
			} else if (httpResponse.getStatusLine().getStatusCode() == 200) {
				AppServiceResult result = new AppServiceResult(true, 200);
				result.setMessageStatus(EntityUtils
						.toString(httpResponse.getEntity()));
				return result;
			} else {
				return new AppServiceResult(false, 500);
			}

		} catch (Exception e) {
			LOGGER.error("[queryMessageStatus] "
					+ httpGet.getURI().getPath() + " failed.", e);
			return new AppServiceResult(false, 500);
		}
	}
	
	/***
	 * 删除消息
	 * @param messageId
	 * @return
	 */
	public AppServiceResult deleteMessage(String messageId)
	{
		HttpDelete httpDelete = new HttpDelete("/message/delete/"
				+ messageId);

		HttpResponse httpResponse = null;
		try {
			httpResponse = invoke(httpDelete);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return new AppServiceResult(true, 200);
			} else {
				return new AppServiceResult(false, httpResponse.getStatusLine()
						.getStatusCode());
			}

		} catch (Exception e) {
			LOGGER.error("[deleteMessage] "
					+ httpDelete.getURI().getPath() + " failed.", e);
			return new AppServiceResult(false, 500);
		}
	}

	/****
	 * 修改消息
	 * @param messageId
	 * @param dataType
	 * @param content
	 * @return
	 */
	public AppServiceResult modifyMessageContent(String messageId,
			String dataType, byte[] content)
	{
		HttpPut httpPut = new HttpPut("/message/update/" + messageId);

		httpPut.addHeader("DataType", dataType);
		httpPut.setHeader("Content-type", "application/octet-stream");
		ByteArrayEntity byteArray = new ByteArrayEntity(content);
		httpPut.setEntity(byteArray);

		HttpResponse httpResponse = null;
		try {
			httpResponse = invoke(httpPut);
			String responseString = EntityUtils.toString(httpResponse
					.getEntity());
			logger.debug("response: {}", responseString);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return new AppServiceResult(true, 200);
			} else {
				return new AppServiceResult(false, httpResponse.getStatusLine()
						.getStatusCode());
			}

		} catch (Exception e) {
			LOGGER.error("[modifyMessageContent] "
					+ httpPut.getURI().getPath() + " failed.", e);
			return new AppServiceResult(false, 500);
		}
	}

	/****
	 * 更改目的地
	 * @param oldCid
	 * @param newCid
	 * @param aid
	 * @return
	 */
	public AppServiceResult changeRecipient(String oldCid,String newCid,String aid)
	{

		HttpPut httpPut = new HttpPut("/change/recipilent/"+oldCid+"/"+newCid);

		//httpPut.addHeader("DataType", dataType);
		httpPut.setHeader("Content-type", "application/octet-stream");
        String json="{\"id\":\""+aid+"\"}";
        ByteArrayEntity byteArray = new ByteArrayEntity(json.getBytes());
		httpPut.setEntity(byteArray);
		HttpResponse httpResponse = null;
		try {
			httpResponse = invoke(httpPut);
			String responseString = EntityUtils.toString(httpResponse
					.getEntity());
			logger.debug("response: {}", responseString);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return new AppServiceResult(true, 200);
			} else {
				return new AppServiceResult(false, httpResponse.getStatusLine()
						.getStatusCode());
			}

		} catch (Exception e) {
			LOGGER.error("[modifyMessageContent] "
					+ httpPut.getURI().getPath() + " failed.", e);
			return new AppServiceResult(false, 500);
		}
	
	}
	
	/***
	 * 主机地址
	 */
	@Override
	protected String getHost() {
		// TODO Auto-generated method stub
		return APP_SERVICE_HOST;
	}
}
