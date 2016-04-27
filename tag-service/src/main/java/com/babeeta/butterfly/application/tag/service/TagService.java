package com.babeeta.butterfly.application.tag.service;

import java.util.List;


/***
 * 
 * @update zeyong.xia
 * @date 2011-9-21
 */
public interface TagService {
	public void registerTag(String clientId, String tagName);

	public void unregisterTag(String clientId, String tagName);

	public List<String> getDeviceListByTag(String tagName);

	public List<String> getTagList(String client);
	
	//以下由zeyong.xia新加
	/****
	 * 新增tag
	 */
	public void registerTag(String clientId, String tagName,String aid);

	/***
	 * 删除tag
	 * @param clientId
	 * @param tagName
	 * @param aid
	 */
	public void unregisterTag(String clientId, String tagName,String aid);
	
	/***
	 * 查询tag信息
	 * @paramtagName tag名称
	 * 
	 * @aid  applicationId
	 */
	public List<String>queryClient(String tagName,String aid);

	/***
	 * 查询tag信息
	 * @clientId cid
	 * 
	 * @aid  applicationId
	 */
	public List<String> queryTag(String clientId,String aid);
}
