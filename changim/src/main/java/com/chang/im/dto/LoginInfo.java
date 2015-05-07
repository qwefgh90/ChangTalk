package com.chang.im.dto;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.json.JSONException;
import org.json.JSONObject;

import com.chang.im.chat.netty.JsonHandler;
import com.chang.im.chat.protocol.JsonTransformer;

public class LoginInfo {
	String token;
	String phone;
	String id;
	String[] roles;
	
	public String[] getRoles() {
		return roles;
	}
	public void setRoles(String[] roles) {
		this.roles = roles;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getId() { 
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
