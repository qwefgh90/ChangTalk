package com.chang.im.dao;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.chang.im.dto.Member;
import com.chang.im.dto.LoginInfo;

@Repository
public class LoginInfoDAO {

	@Autowired
	RedisTemplate<String,LoginInfo> redisTemplateForUserinfo;
	
	@Resource(name="redisTemplateForUserinfo")
    private ValueOperations<String, LoginInfo> valueOps;

	public void insertUserInfo(LoginInfo userinfo){
		String key = key(userinfo.getToken());
		valueOps.set(key, userinfo);
	}

	public boolean isExistsUserInfo(String token){
		String key = key(token);
		return redisTemplateForUserinfo.hasKey(key);
	}
	
	public LoginInfo getUserInfo(String token){
		String key =  key(token);
		return valueOps.get(key);
	}
	
	public void deleteUserInfo(String token){
		String key = key(token);
		redisTemplateForUserinfo.delete(key);
	}
	
	private String key(String token){
		return "LoginInfo:"+token;
	}
}
