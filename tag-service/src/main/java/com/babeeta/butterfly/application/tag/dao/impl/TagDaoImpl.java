package com.babeeta.butterfly.application.tag.dao.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.tag.dao.TagDao;
import com.babeeta.butterfly.application.tag.entity.TagInfo;
import com.google.code.morphia.query.Query;

public class TagDaoImpl extends BasicDaoImpl implements TagDao {
	private final static Logger logger = LoggerFactory
			.getLogger(TagDaoImpl.class);

	public TagDaoImpl() {
		datastore = morphia.createDatastore(mongo, "tag");
		datastore.ensureIndexes();
	}

	@Override
	public void addTag(TagInfo tag) {
		Query<TagInfo> query =
				datastore.createQuery(TagInfo.class)
						.filter("clientId", tag.getClientId())
						.filter("tagName", tag.getTagName());
		if (query.get() == null) {
			datastore.save(tag);
		} else {
			logger.debug("[tag dao]addTag | tag[{}] on device[{}] exist.",
					tag.getTagName(), tag.getClientId());
		}
	}

	@Override
	public void removeTag(TagInfo tag) {
		Query<TagInfo> query =
				datastore.createQuery(TagInfo.class)
						.filter("clientId", tag.getClientId())
						.filter("tagName", tag.getTagName());
		if (query.get() != null) {
			datastore.delete(query);
		} else {
			logger.debug(
					"[tag dao]removeTag | not found tag[{}] on device[{}].",
					tag.getTagName(), tag.getClientId());
		}
	}

	@Override
	public List<TagInfo> queryDevice(String tagName) {
		Query<TagInfo> query =
				datastore.createQuery(TagInfo.class)
						.filter("tagName", tagName);
		if (query.get() != null) {
			return query.asList();
		} else {
			logger.debug(
					"[tag dao]queryDevice | not found device has tag[{}].",
					tagName);
			return null;
		}
	}

	@Override
	public List<TagInfo> queryTag(String clientId) {
		Query<TagInfo> query =
				datastore.createQuery(TagInfo.class)
						.filter("clientId", clientId);
		if (query.get() != null) {
			return query.asList();
		} else {
			logger.debug(
					"[tag dao]queryDevice | not found tag on device[{}].",
					clientId);
			return null;
		}
	}
	
	/***
	 * 查询tag信息
	 * @paramtagName tag名称
	 * 
	 * @aid  applicationId
	 */
	@Override
	public List<TagInfo> queryClient(String tagName,String aid)
	{
		Query<TagInfo> query =
			datastore.createQuery(TagInfo.class)
					.filter("tagName", tagName).filter("aid", aid);
	if (query.get() != null) {
		return query.asList();
	} else {
		logger.debug(
				"[tag dao]queryDevice | not found device has tag[{}].",
				tagName);
		return null;
	}
	}

	/***
	 * 查询tag信息
	 * @clientId cid
	 * 
	 * @aid  applicationId
	 */
	@Override
	public List<TagInfo> queryTag(String clientId,String aid)
	{
		Query<TagInfo> query =
			datastore.createQuery(TagInfo.class)
					.filter("clientId", clientId).filter("aid", aid);
	if (query.get() != null) {
		return query.asList();
	} else {
		logger.debug(
				"[tag dao]queryDevice | not found tag on device[{}].",
				clientId);
		return null;
	}
	}

}
