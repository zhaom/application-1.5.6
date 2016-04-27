package com.babeeta.butterfly.application.words.controller;



import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.babeeta.butterfly.application.words.service.IWordsService;


@Path("/words")
@Controller
public class WebController {

	private final static Logger logger = LoggerFactory
	.getLogger(WebController.class);
	
	private IWordsService wordsServiceImpl;
	
	/***
	 * 新增敏感词
	 * @return
	 */
	@POST
	@Path("/addword")
	public Response addWords(String words)
	{
		try {
			words=URLDecoder.decode(words,"utf-8");
			words=words.substring(words.indexOf("=")+1);
			System.out.println("words= "+words);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		words=words.replaceAll("[~,!,@,#,$,%,^,&,\\*,\\(,\\),\\-,——,\\+,=,:,\\?,\\？,￥]", "");
		if(words!=null)
		{
			this.wordsServiceImpl.addWords(words);
		}
		else
		{
			logger.error("add words error");
			return Response.serverError().build();
		}
		logger.info("add words success");
		return Response.ok().build();
	}
	
	@DELETE
	@Path("/removeword")
	@Produces("application/json")
	public boolean removeWords(String words)
	{
		words=words.replaceAll("[~,!,@,#,$,%,^,&,\\*,\\(,\\),\\-,——,\\+,=,:,\\?,\\？,￥]", "");
		
		if(words!=null)
		{		
			this.wordsServiceImpl.removeWord(words);
		}
		else
		{
			logger.error("add words error");
			return false;
		}
		logger.info("add words success");
		return true;
	}
	
	/***
	 * 新增敏感词前验证
	 * @param words
	 * @return
	 */
	@POST
	@Path("/validate")
	public Response ajaxVilidateWords(String word)
	{
	    System.out.println("word= "+word);
	    boolean flag=this.wordsServiceImpl.filterWord(word);
	    return Response.ok(new Boolean(flag)).build();	    
	}
	public void setWordsServiceImpl(IWordsService wordsServiceImpl) {
		this.wordsServiceImpl = wordsServiceImpl;
	}
	
}
