package com.babeeta.butterfly.application.third.resource;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Controller;

import com.babeeta.butterfly.application.third.service.auth.AuthFailedReason;
import com.babeeta.butterfly.application.third.service.auth.AuthResult;
import com.babeeta.butterfly.application.third.service.auth.AuthService;

@Controller
@Path("/1/api/test")
public class ResourceTest {

	private AuthService authService;

	@GET
	@Path("/auth/{pathParam}")
	@Produces("application/json")
	public Response auth(@PathParam("pathParam") int pathParam,
			@HeaderParam("headerParam") String headerParam) {
		System.out.println("pathParam:" + pathParam);
		System.out.println("headerParam:" + headerParam);
		System.out.println("authService:"
				+ authService);

		AuthResult authResult = new AuthResult(false, AuthFailedReason.freezed);

		return Response.ok(authResult).build();
	}

	public void setAuthService(
			AuthService authService) {
		this.authService = authService;
	}
}
