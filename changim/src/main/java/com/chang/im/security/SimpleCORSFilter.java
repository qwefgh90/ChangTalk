package com.chang.im.security;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

/**
 * Any Host Allow
 * https://spring.io/guides/gs/rest-service-cors/
 * not use until the problems occur
 * @author cheochangwon
 *
 */
//@Component
public class SimpleCORSFilter implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletResponse res = (HttpServletResponse) response;
		res.setHeader("Access-Control-Allow-Origin", "*");	//Any Host
		res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");	//allow method
		res.setHeader("Access-Control-Max-Age", "3600");	
		res.setHeader("Access-Control-Allow-Headers", "x-requested-with");	
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}


}
