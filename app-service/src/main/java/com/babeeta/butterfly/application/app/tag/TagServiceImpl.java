package com.babeeta.butterfly.application.app.tag;

import net.sf.json.JSONArray;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagServiceImpl extends AbstractHttpRPCService implements
		TagService {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TagServiceImpl.class);
	private static final String TAG_SERVICE_HOST = "tag.app";

	@Override
	protected String getHost() {
		return TAG_SERVICE_HOST;
	}

	@Override
	public TagResult listDevice(String groupTag) {
		HttpGet httpGet = new HttpGet("/api/get-tag-device-list/"
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
