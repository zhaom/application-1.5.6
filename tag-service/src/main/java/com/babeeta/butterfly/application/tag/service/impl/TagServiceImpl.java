package com.babeeta.butterfly.application.tag.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.tag.dao.TagDao;
import com.babeeta.butterfly.application.tag.entity.TagInfo;
import com.babeeta.butterfly.application.tag.service.TagService;

/****
 * 
 * @update zeyong.xia
 * @date 2011-9-21
 */
public class TagServiceImpl implements TagService {
	private final static Logger logger = LoggerFactory
			.getLogger(TagServiceImpl.class);
	private TagDao tagDao;

	public void setTagDao(TagDao tagDao) {
		this.tagDao = tagDao;
	}

	@Override
	public void registerTag(String clientId, String tagName) {
		logger.debug("[tag service]registerTag set tag[{}] to [{}].", tagName,
				clientId);
		TagInfo tag = new TagInfo();
		tag.setClientId(clientId);
		tag.setTagName(tagName);
		tagDao.addTag(tag);
	}

	@Override
	public void unregisterTag(String clientId, String tagName) {
		logger.debug("[tag service]unregisterTag remove tag[{}] from [{}].",
				tagName,
				clientId);
		TagInfo tag = new TagInfo();
		tag.setClientId(clientId);
		tag.setTagName(tagName);
		tagDao.removeTag(tag);
	}

	@Override
	public List<String> getDeviceListByTag(String tagName) {
		logger.debug(
				"[tag service]getDeviceListByTag list all device has tag [{}].",
				tagName);
		List<TagInfo> deviceList = tagDao.queryDevice(tagName);

		if (deviceList != null) {
			logger.debug("Get [{}] devices which has tag[{}].",
					deviceList.size(), tagName);
			List<String> result = new ArrayList<String>();
			for (TagInfo record : deviceList) {
				if (!result.contains(record.getClientId())) {
					result.add(record.getClientId());
				}
			}
			return result;
		} else {
			logger.debug("no device has tag[{}].", tagName);
			return null;
		}
	}

	@Override
	public List<String> getTagList(String client) {
		logger.debug(
				"[tag service]getDeviceListByTag list all tags on device[{}].",
				client);
		List<TagInfo> tagList = tagDao.queryTag(client);

		if (tagList != null) {
			logger.debug("Found [{}] tag on [{}].",
					tagList.size(), client);
			List<String> result = new ArrayList<String>();
			for (TagInfo record : tagList) {
				if (!result.contains(record.getTagName())) {
					result.add(record.getTagName());
				}
			}
			if (result.size() > 0) {
				return result;
			} else {
				logger.debug("No other tag except userId.");
				return null;
			}
		} else {
			logger.error("Invalid client [{}].", client);
			return null;
		}
	}
	//////
	/****
	 * 新增tag
	 */
	public void registerTag(String clientId, String tagName,String aid)
	{

		logger.debug("[tag service]registerTag set tag[{}] to [{}].", tagName,
				clientId);
		TagInfo tag = new TagInfo();
		tag.setClientId(clientId);
		tag.setTagName(tagName);
		tag.setAid(aid);
		tagDao.addTag(tag);
	
	}

	/***
	 * 删除tag
	 * @param clientId
	 * @param tagName
	 * @param aid
	 */
	public void unregisterTag(String clientId, String tagName,String aid)
	{

		logger.debug("[tag service]unregisterTag remove tag[{}] from [{}].",
				tagName,
				clientId);
		TagInfo tag = new TagInfo();
		tag.setClientId(clientId);
		tag.setTagName(tagName);
		tag.setAid(aid);
		tagDao.removeTag(tag);
	
	}
	
	/***
	 * 查询tag信息
	 * @paramtagName tag名称
	 * 
	 * @aid  applicationId
	 */
	public List<String> queryClient(String tagName,String aid)
	{
		logger.debug(
				"[tag service]getDeviceListByTag list all device has tag [{}].",
				tagName);
		List<TagInfo> deviceList = tagDao.queryClient(tagName,aid);

		if (deviceList != null) {
			logger.debug("Get [{}] devices which has tag[{}].",
					deviceList.size(), tagName);
			List<String> result = new ArrayList<String>();
			for (TagInfo record : deviceList) {
				if (!result.contains(record.getClientId())) {
					result.add(record.getClientId());
				}
			}
			return result;
		} else {
			logger.debug("no device has tag[{}].", tagName);
			return null;
		}
	}

	/***
	 * 查询tag信息
	 * @clientId cid
	 * 
	 * @aid  applicationId
	 */
	public List<String> queryTag(String client,String aid)
	{
		logger.debug(
				"[tag service]getDeviceListByTag list all tags on device[{}].",
				client);
		List<TagInfo> tagList = tagDao.queryTag(client,aid);

		if (tagList != null) {
			logger.debug("Found [{}] tag on [{}].",
					tagList.size(), client);
			List<String> result = new ArrayList<String>();
			for (TagInfo record : tagList) {
				if (!result.contains(record.getTagName())) {
					result.add(record.getTagName());
				}
			}
			if (result.size() > 0) {
				return result;
			} else {
				logger.debug("No other tag except userId.");
				return null;
			}
		} else {
			logger.error("Invalid client [{}].", client);
			return null;
		}
	}
}
