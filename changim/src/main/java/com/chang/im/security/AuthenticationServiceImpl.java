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
import com.chang.im.dto.LoginInfo;
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
				if(memberService.getLoginState(id) && memberService.isValidIdAndPassword(id, password)){
					String token = memberService.getTokenListItem(id).getToken();
					if(null != token && checkToken(token))
						return token;
				}
				Authentication authentication = new UsernamePasswordAuthenticationToken(id, password);
				authentication = authenticationManager.authenticate(authentication);
				UserDetails userDetails = (UserDetails)authentication.getPrincipal();
				if(userDetails != null && authentication.isAuthenticated()){
					SecurityContextHolder.getContext().setAuthentication(authentication);
					MemberContext mContext = ((MemberContext)userDetails);
					//토큰 생성
					Member member = mContext.getMember();
					if(memberService.login(member))
						return memberService.getTokenListItem(member.getId()).getToken();
				}
			}catch ( AuthenticationException e){
				System.out.println(" *** AuthenticationServiceImpl.authenticate - FAILED: " + e.toString());
			}
		}
		return null;
	}

	@Override
	public boolean checkToken(String token) {
		if(token == null)
			return false;

		boolean result = false;
		if(memberService.isExistToken(token) == true){
			if(memberService.updateTokenDate(token)){
				LoginInfo info = memberService.getUserInfo(token);
				Member member = new Member();
				member.setId(info.getId());
				member.setPassword(null);
				member.setRoles(info.getRoles());
				MemberContext context = new MemberContext(member);

				UsernamePasswordAuthenticationToken userpwToken = new UsernamePasswordAuthenticationToken(context.getUsername(),null,context.getAuthorities());

				SecurityContextHolder.getContext().setAuthentication(userpwToken);
				result = true;
			}
		}

		return result;
	}

	@Override
	public boolean logout(String token) {
		boolean result = false;
		if(memberService.logout(token)){
			SecurityContextHolder.clearContext();
			result = true;
		}
		return result;
	}

	@Override
	public UserDetails currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		return null;
	}


}
