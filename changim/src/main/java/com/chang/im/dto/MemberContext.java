package com.chang.im.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class MemberContext implements UserDetails {

	Member member;
	
	public MemberContext(Member member){
		this.member = member;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		for (String role : member.getRoles()) {
			authorities.add(new SimpleGrantedAuthority(role));
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return member.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return member.getId();
	}

	@Override
	public boolean isAccountNonExpired() {
		//계정 만료
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		//최대 로그인 횟수 초과 할 경우 처리??
		boolean result = true;
		return result;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		//비밀번호 만료?
		return true;
	}

	@Override
	public boolean isEnabled() {
		//사용가능한가?
		return true;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

}
