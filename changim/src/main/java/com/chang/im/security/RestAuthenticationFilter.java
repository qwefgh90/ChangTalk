package com.chang.im.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

public class RestAuthenticationFilter extends GenericFilterBean {

	private static final String LOGIN_URL_V1 = "/v1/auth/login";
	private static final String LOGOUT_URL_V1 = "/v1/auth/logout";

	private static final String HEADER_TOKEN = "X-Auth-Token";
	private static final String HEADER_USERNAME = "X-Id";
	private static final String HEADER_PASSWORD = "X-Password";

	@Autowired
	AuthenticationService authenticationService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		doFilterv1(httpRequest,httpResponse,chain);
	}
	/**
	 * rest version 1 을 위한 필터
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	public void doFilterv1(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		String extractedId = request.getHeader(HEADER_USERNAME);
		String extractedPassword = request.getHeader(HEADER_PASSWORD);
		String extractedToken = request.getHeader(HEADER_TOKEN);

		if(currentLink(request).equals(LOGIN_URL_V1) && request.getMethod() == "POST"){
			String createdToken = authenticationService.authenticate(extractedId, extractedPassword);
			if(createdToken != null){
				//login success
				response.setHeader(HEADER_TOKEN, createdToken);
				chain.doFilter(request, response);
			}else{
				//fail
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}else{

			boolean authentication = checkAuthenticateForHttp(extractedToken);

			if(!authentication){
				//auth fail
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}else{

				if(currentLink(request).equals(LOGOUT_URL_V1) && request.getMethod() == "POST"){
					//logout
					logoutForHttp(extractedToken);
				}else{
					//do filter
					chain.doFilter(request, response);
				}
			}
		}
	}

	public boolean checkAuthenticateForHttp(String token){
		boolean result = false;
		return result;
	}

	public void logoutForHttp(String token){

	}

	public String currentLink(HttpServletRequest httpRequest) {
		if (httpRequest.getPathInfo() == null) {
			return httpRequest.getServletPath();
		}
		return httpRequest.getServletPath() + httpRequest.getPathInfo();
	}
}
