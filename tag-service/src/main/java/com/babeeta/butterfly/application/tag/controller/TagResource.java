package com.babeeta.butterfly.application.tag.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.babeeta.butterfly.application.tag.service.TagService;
/***
 * clientId for tag
 * @author zeyong.xia
 * @date 2011-9-27
 */
@Path("/")
@Component("tagResource")
public class TagResource {

	private final static Logger logger = LoggerFactory
			.getLogger(TagResource.class);
	private TagService tagService;
	
	/***
	 * 为Client打Tag
	 * @return
	 */
	@Path("/api/set-tag-for-client/{aid}/{cid}")
	@Consumes("text/plain;charset=UTF-8")
	@Produces("text/plain;charset=UTF-8")
	public Response setTagForClient(@PathParam("cid")String cid,@PathParam("aid")String aid,
			                         String tagName)
	{
		logger.debug("[tag resource]setTagForclient {} to {}.", tagName,
				cid);
		if (cid == null) {
			return Response.status(404).build();
		}
		if (aid == null) {
			return Response.status(404).build();
		}
		if (tagName == null) {
			return Response.status(422).build();
		}
		if (tagName.indexOf(",") > -1) {
			String[] tagArray = tagName.split(",");
			for (String tag : tagArray) {
				tagService.registerTag(cid, tag,aid);
			}
		} else {
			tagService.registerTag(cid, tagName,aid);
		}
		return Response.status(200).build();
	}
	
	/***
	 * 移除Client Tag
	 * @return
	 */
	@DELETE
	@Path("/api/remove-client-tag/{aid}/{cid}/{tagName}")
	public Response removeTagForClient(@PathParam("cid")String cid,
			                         @PathParam("tagName")String tagName,@PathParam("aid")String aid)
	{

		logger.debug("[tag resource]removeTagForClient {} from {}.", tagName,
				cid);
		if (cid == null) {
			return Response.status(404).build();
		}
		if (aid == null) {
			return Response.status(404).build();
		}
		if (tagName == null) {
			return Response.status(422).build();
		}

		if (tagName.indexOf(",") > -1) {
			String[] tagArray = tagName.split(",");
			for (String tag : tagArray) {
				tagService.unregisterTag(cid, tag,aid);
			}
		} else {
			tagService.unregisterTag(cid, tagName,aid);
		}
		return Response.status(200).build();
	
	}
	
	/***
	 * 得到TagName列表
	 * @return
	 */
	@GET
	@Path("/api/get-client-tag-list/{cid}/{aid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClientTagList(@PathParam("cid")String cid,@PathParam("aid")String aid)
	{

		logger.debug("[tag resource]getClientTagList of {}.", cid);
		if (cid == null) {
			return Response.status(404).build();
		}
		if (aid == null) {
			return Response.status(404).build();
		}
		List<String> result = new ArrayList<String>();
		List<String> tagList = tagService.queryTag(cid,aid);
		logger.debug("[{}] has tag :[{}].", cid,
						tagList.toString());
		for (String tagName : tagList) {
			if (!result.contains(tagName)) {
				result.add(tagName);
				logger.debug("[{}] has tag :[{}].", cid,
								tagName);
			}
		}
		if (result.size() > 0) {
			logger.debug("[{}] has tag :[{}].", cid,
						result.toString());
			return Response.status(200).entity(result).build();
		} else {
			return Response.status(204).build();
		}
	
	}
	/***
	 * 得到ClientId列表
	 * @return
	 */
	@GET
	@Path("/api/get-tag-client-list/{aid}/{tagName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClientListFromTag(@PathParam("tagName")String tagName,@PathParam("aid")String aid)
	{

		logger.debug("[tag resource]getClientListFromTag of {}.", tagName);
		if (tagName == null) {
			return Response.status(404).build();
		}
		if (aid == null) {
			return Response.status(404).build();
		}
		List<String> result = new ArrayList<String>();
		if (tagName.indexOf(",") > -1) {
			String[] tagList = tagName.split(",");
			for (String tag : tagList) {
				List<String> deviceList = tagService.queryClient(tagName, aid);
				logger.debug("device: [{}] have tag [{}].",
						deviceList.toString(),
						tag);
				for (String device : deviceList) {
					if (!result.contains(device)) {
						result.add(device);
					}
				}
			}
		} else {
			List<String> deviceList = tagService.queryClient(tagName, aid);
			logger.debug("device: [{}] have tag [{}].",
						deviceList.toString(),
						tagName);
			for (String device : deviceList) {
				if (!result.contains(device)) {
					result.add(device);
				}
			}
		}

		if (result.size() > 0) {
			return Response.status(200).entity(result).build();
		} else {
			return Response.status(404).build();
		}
	
	}
	
	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}
}
