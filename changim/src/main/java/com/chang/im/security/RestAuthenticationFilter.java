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

import com.chang.im.service.MemberService;

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
	 * 로그인/로그아웃은 HTTP처리 및 응답 (/v1/auth/.* 여기서 처리됨)
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

		boolean authentication = checkAuthenticateForHttp(extractedToken);

		//로그인
		if (currentLink(request).equals(LOGIN_URL_V1) && request.getMethod() == "POST") {
			if(authentication == false){
				String createdToken = authenticationService.authenticate(extractedId, extractedPassword);
				if(createdToken != null){
					response.setHeader(HEADER_TOKEN, createdToken);
					response.sendError(HttpServletResponse.SC_OK);
				}else{
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				}
			} else {
				response.setHeader(HEADER_TOKEN, extractedToken);
				response.sendError(HttpServletResponse.SC_OK);
			}
			//로그아웃
		} else if (currentLink(request).equals(LOGOUT_URL_V1) && request.getMethod() == "POST") {
			if (authentication) {
				if(logoutForHttp(extractedToken))
					response.sendError(HttpServletResponse.SC_OK);
				else
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}

		} else {
			chain.doFilter(request, response);
		}
	}

	public boolean checkAuthenticateForHttp(String token){
		return authenticationService.checkToken(token);
	}

	public boolean logoutForHttp(String token){
		return authenticationService.logout(token);
	}

	public String currentLink(HttpServletRequest httpRequest) {
		if (httpRequest.getPathInfo() == null) {
			return httpRequest.getServletPath();
		}
		return httpRequest.getServletPath() + httpRequest.getPathInfo();
	}
}
