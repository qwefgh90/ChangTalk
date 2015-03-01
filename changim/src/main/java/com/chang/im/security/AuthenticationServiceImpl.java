package com.chang.im.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.chang.im.dto.Member;
import com.chang.im.dto.MemberContext;
import com.chang.im.service.MemberService;

public class AuthenticationServiceImpl implements AuthenticationService{

	@Autowired
	MemberService memberService;

	@Autowired
	AuthenticationManager authenticationManager;

	/**
	 * 인증 서비스, 토큰 반환
	 * return: Token
	 */
	@Override
	public String authenticate(String id, String password) {
		if(id != null && password != null){
			try {
			Authentication authentication = new UsernamePasswordAuthenticationToken(id, password);
			authentication = authenticationManager.authenticate(authentication);
			UserDetails userDetails = (UserDetails)authentication.getPrincipal();
			if(userDetails != null && authentication.isAuthenticated()){
				SecurityContextHolder.getContext().setAuthentication(authentication);
				MemberContext mContext = ((MemberContext)userDetails);
				//토큰 생성
				Member member = mContext.getMember();
				if(memberService.login(member))
					return member.getToken();
			}
			}catch ( AuthenticationException e){
				System.out.println(" *** AuthenticationServiceImpl.authenticate - FAILED: " + e.toString());
			}
		}
		return null;
	}

	@Override
	public boolean checkToken(String token) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logout(String token) {
		// TODO Auto-generated method stub

	}

	@Override
	public UserDetails currentUser() {
		// TODO Auto-generated method stub
		return null;
	}


}
