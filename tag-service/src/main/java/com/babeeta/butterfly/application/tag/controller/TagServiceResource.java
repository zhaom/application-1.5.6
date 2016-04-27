package com.babeeta.butterfly.application.tag.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.babeeta.butterfly.application.tag.service.TagService;

@Path("/")
@Scope(value = "prototype")
@Component("tagServiceResource")
public class TagServiceResource {
	private final static Logger logger = LoggerFactory
			.getLogger(TagServiceResource.class);
	private TagService tagService;

	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}

	@PUT
	@Path("/api/set-tag-for-device/{clientId}")
	@Consumes("text/plain;charset=UTF-8")
	@Produces("text/plain;charset=UTF-8")
	public Response setTagForDevice(
			@PathParam("clientId") String clientId,
			String tagName) {
		logger.debug("[tag resource]setTagForDevice {} to {}.", tagName,
				clientId);
		if (clientId == null) {
			return Response.status(404).build();
		}
		if (tagName == null) {
			return Response.status(422).build();
		}
		if (tagName.indexOf(",") > -1) {
			String[] tagArray = tagName.split(",");
			for (String tag : tagArray) {
				tagService.registerTag(clientId, tag);
			}
		} else {
			tagService.registerTag(clientId, tagName);
		}
		return Response.status(200).build();
	}

	@DELETE
	@Path("/api/remove-device-tag/{clientId}/{tagName}")
	public Response removeUserDevice(
			@PathParam("clientId") String clientId,
			@PathParam("tagName") String tagName) {
		logger.debug("[tag resource]removeUserDevice {} from {}.", tagName,
				clientId);
		if (clientId == null) {
			return Response.status(404).build();
		}
		if (tagName == null) {
			return Response.status(422).build();
		}

		if (tagName.indexOf(",") > -1) {
			String[] tagArray = tagName.split(",");
			for (String tag : tagArray) {
				tagService.unregisterTag(clientId, tag);
			}
		} else {
			tagService.unregisterTag(clientId, tagName);
		}
		return Response.status(200).build();
	}

	@GET
	@Path("/api/get-device-tag-list/{clientId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeviceTagList(
			@PathParam("clientId") String clientId) {
		logger.debug("[tag resource]getDeviceTagList of {}.", clientId);
		if (clientId == null) {
			return Response.status(404).build();
		}

		List<String> result = new ArrayList<String>();
		List<String> tagList = tagService.getTagList(clientId);
		logger.debug("[{}] has tag :[{}].", clientId,
						tagList.toString());
		for (String tagName : tagList) {
			if (!result.contains(tagName)) {
				result.add(tagName);
				logger.debug("[{}] has tag :[{}].", clientId,
								tagName);
			}
		}
		if (result.size() > 0) {
			logger.debug("[{}] has tag :[{}].", clientId,
						result.toString());
			return Response.status(200).entity(result).build();
		} else {
			return Response.status(204).build();
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	@GET
	@Path("/api/get-tag-device-list/{tagName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeviceListOfTag(
			@PathParam("tagName") String tagName) {
		logger.debug("[tag resource]getDeviceListOfTag of {}.", tagName);
		if (tagName == null) {
			return Response.status(404).build();
		}
		List<String> result = new ArrayList<String>();
		if (tagName.indexOf(",") > -1) {
			String[] tagList = tagName.split(",");
			for (String tag : tagList) {
				List<String> deviceList = tagService.getDeviceListByTag(tag);
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
			List<String> deviceList = tagService.getDeviceListByTag(tagName);
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
}
