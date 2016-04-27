package com.babeeta.butterfly.application.third.service.tag;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.HttpHeaders;

import net.sf.json.JSONArray;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.babeeta.butterfly.application.third.service.AbstractHttpRPCService;

/***
 * oppo Tag定制
 * @author zeyong.xia
 * @date 2011-9-23
 */
public class OppoTagServiceImpl extends AbstractHttpRPCService implements
		OppoTagService {

	
	private static final Logger LOGGER = LoggerFactory
	.getLogger(OppoTagServiceImpl.class);
	
	private static final String TAG_SERVICE_HOST = "app.";
	@Override
	protected String getHost() {
		// TODO Auto-generated method stub
		return TAG_SERVICE_HOST;
	}


	/***
	 * 打tag
	 * @param clientId
	 * @param aid
	 * @param groupTag
	 * @return
	 */
	public TagResult setGroupTag(String clientId, String aid,String groupTag)
	{

		HttpPut httpPut = new HttpPut("/api/set-tag-for-client/"+aid+"/"+clientId);

		httpPut.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_PLAIN);

		try {
			httpPut.setEntity(new StringEntity(groupTag));
		} catch (UnsupportedEncodingException e1) {
			LOGGER.error("set entity failed.", e1);
			return new TagResult(false, 500);
		}

		HttpResponse httpResponse = null;
		try {
			httpResponse = invoke(httpPut);
			if (httpResponse.getStatusLine().getStatusCode() == 404) {
				return new TagResult(false, 404);
			} else if (httpResponse.getStatusLine().getStatusCode() == 422) {
				return new TagResult(false, 422);
			} else if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return new TagResult(true, 200);
			} else {
				return new TagResult(false, 500);
			}

		} catch (Exception e) {
			LOGGER.error("[setGroupTag] "
					+ httpPut.getURI().getPath() + " failed.", e);
			return new TagResult(false, 500);
		}
	
	}

	/***
	 * 移除tag
	 * @param clientId
	 * @param aid
	 * @param groupTag
	 * @return
	 */
	public TagResult removeGroupTag(String clientId,String aid, String groupTag)
	{
		HttpDelete httpDelete = new HttpDelete("/api/remove-client-tag/"+aid+"/"+clientId+"/"+groupTag);

		HttpResponse httpResponse = null;
		try {
			httpResponse = invoke(httpDelete);
			if (httpResponse.getStatusLine().getStatusCode() == 404) {
				return new TagResult(false, 404);
			} else if (httpResponse.getStatusLine().getStatusCode() == 422) {
				return new TagResult(false, 422);
			} else if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return new TagResult(true, 200);
			} else {
				return new TagResult(false, 500);
			}

		} catch (Exception e) {
			LOGGER.error("[removeGroupTag] "
					+ httpDelete.getURI().getPath() + " failed.", e);
			return new TagResult(false, 500);
		}
	}

	/***
	 * 查询tag信息
	 * @param clientId
	 * @param aid
	 * @return
	 */
	public TagResult listGroupTag(String clientId,String aid)
	{
		HttpGet httpGet = new HttpGet("/api/get-client-tag-list/"+aid+"/"
				+ clientId);

		HttpResponse httpResponse = null;
		try {
			httpResponse = invoke(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == 404) {
				return new TagResult(false, 404);
			} else if (httpResponse.getStatusLine().getStatusCode() == 204) {
				return new TagResult(true, 204);
			} else if (httpResponse.getStatusLine().getStatusCode() == 200) {
				TagResult result = new TagResult(true, 200);
				String[] tagList = (String[]) JSONArray.fromObject(
						EntityUtils
								.toString(httpResponse.getEntity()))
						.toArray(
								new String[] {});
				result.setStringList(tagList);
				return result;
			} else {
				return new TagResult(false, 500);
			}

		} catch (Exception e) {
			LOGGER.error("[listGroupTag] "
					+ httpGet.getURI().getPath() + " failed.", e);
			return new TagResult(false, 500);
		}
	}

	/***
	 * 查询tag信息
	 * @param groupTag
	 * @return
	 */
	public TagResult listDevice(String groupTag,String aid)
	{
		HttpGet httpGet = new HttpGet("/api/get-tag-client-list/"+aid+"/"
				+ groupTag);

		HttpResponse httpResponse = null;
		try {
			httpResponse = invoke(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == 404) {
				return new TagResult(false, 404);
			} else if (httpResponse.getStatusLine().getStatusCode() == 200) {
				TagResult result = new TagResult(true, 200);
				String[] tagList = (String[]) JSONArray.fromObject(
						EntityUtils
								.toString(httpResponse.getEntity()))
						.toArray(
								new String[] {});
				result.setStringList(tagList);
				return result;
			} else {
				return new TagResult(false, 500);
			}
		} catch (Exception e) {
			LOGGER.error("[listDevice] "
					+ httpGet.getURI().getPath() + " failed.", e);
			return new TagResult(false, 500);
		}
	}
}
