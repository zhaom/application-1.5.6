package com.babeeta.butterfly.application.third.resource;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.babeeta.butterfly.application.third.service.auth.AuthResult;
import com.babeeta.butterfly.application.third.service.auth.AuthService;

public class AuthFilter implements Filter {

	private final static Logger logger = LoggerFactory
			.getLogger(AuthInterceptor.class);
	private static AuthService authService;

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
System.out.println("***"+request
				.getHeader("authorization").toString());
		String[] authContent = getAuthContent(request
				.getHeader("authorization"));

		if (isAuthContentEmpty(authContent)) {
			logger.debug("empty Authorization.");
			response.setStatus(401);
			response.getWriter().write("Unauthorized");
			return;
		}

		AuthResult authResult = authService.authenticate(authContent[0],
				authContent[1]);

		if (!authResult.isSuccess()) {
			logger.error("auth failed with appId:{} appKey:{}", authContent[0],
					authContent[1]);
			response.setStatus(401);
			response.getWriter().write("Unauthorized");
			return;
		}

		filterChain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		WebApplicationContext context = WebApplicationContextUtils
				.getWebApplicationContext(filterConfig.getServletContext());
		authService = (AuthService) context.getBean("authService");
	}

	private String[] getAuthContent(String authorization) {
		try {
			String base64Content = authorization.split(" ")[1];
			String authContent = new String(Base64.decodeBase64(base64Content),
					"UTF-8");
			return authContent.split(":");
		} catch (Exception e) {
			logger.error("[authorization header] {}", e.getMessage());
			return null;
		}
	}

	private boolean isAuthContentEmpty(String[] authContent) {

		if (authContent == null || authContent.length != 2
				|| authContent[0].trim().length() == 0
				|| authContent[1].trim().length() == 0) {
			return true;
		}
		return false;
	}

}
