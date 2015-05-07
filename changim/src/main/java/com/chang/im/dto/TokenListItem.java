package com.chang.im.dto;

import org.json.JSONObject;

import com.chang.im.chat.protocol.JsonTransformer;

public class TokenListItem {
	String id;
	String token;
	long expire;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public long getExpire() {
		return expire;
	}
	public void setExpire(long expire) {
		this.expire = expire;
	}
}
