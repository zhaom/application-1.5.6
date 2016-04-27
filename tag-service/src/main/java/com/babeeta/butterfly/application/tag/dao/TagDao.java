package com.babeeta.butterfly.application.tag.dao;

import java.util.List;

import com.babeeta.butterfly.application.tag.entity.TagInfo;

public interface TagDao {
	public void addTag(TagInfo tag);

	public void removeTag(TagInfo tag);

	public List<TagInfo> queryDevice(String tagName);

	public List<TagInfo> queryTag(String clientId);
	
	//以下油zeyong.xia添加
	
	/***
	 * 查询tag信息
	 * @paramtagName tag名称
	 * 
	 * @aid  applicationId
	 */
	public List<TagInfo> queryClient(String tagName,String aid);

	/***
	 * 查询tag信息
	 * @clientId cid
	 * 
	 * @aid  applicationId
	 */
	public List<TagInfo> queryTag(String clientId,String aid);
}
