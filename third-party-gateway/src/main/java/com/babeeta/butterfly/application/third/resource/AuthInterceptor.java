package com.babeeta.butterfly.application.third.resource;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.third.service.auth.AuthResult;
import com.babeeta.butterfly.application.third.service.auth.AuthService;

@SecurityPrecedence
@ServerInterceptor
public class AuthInterceptor implements PreProcessInterceptor {
	private final static Logger logger = LoggerFactory
			.getLogger(AuthInterceptor.class);
	private AuthService authService;

	public void setAuthService(AuthService authService) {
		this.authService = authService;
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

	@Override
	public ServerResponse preProcess(HttpRequest request, ResourceMethod method) {
		String[] authContent = getAuthContent(request.getHttpHeaders()
				.getRequestHeaders().getFirst("authorization"));

		if (isAuthContentEmpty(authContent)) {
			return ServerResponse
					.copyIfNotServerResponse(Response.status(
							Status.UNAUTHORIZED).build());
		}
		AuthResult authResult = authService.authenticate(authContent[0],
				authContent[1]);

		if (!authResult.isSuccess()) {
			return ServerResponse
					.copyIfNotServerResponse(Response.status(
							Status.UNAUTHORIZED).build());
		}
		return null;
	}
}
