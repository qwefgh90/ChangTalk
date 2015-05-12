package com.chang.im.dto;

public class Member {
	String id;
	String phone;
	String password;
	String[] roles;
	
	public static final String MEMBER_ROLE = "MEMBER_ROLE";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String[] getRoles() {
		return roles;
	}
	public void setRoles(String... roles) {
		this.roles = roles;
	}
	
}
