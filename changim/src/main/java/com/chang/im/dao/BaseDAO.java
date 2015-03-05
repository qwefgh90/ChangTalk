package com.chang.im.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BaseDAO {
	
	@Autowired
	RedisTemplate<String,String> redisTemplate;
	
	public void delete(String key){
		redisTemplate.delete(key);
	}
	public String getValue(String key){
		return redisTemplate.opsForValue().get(key);
	}
	
	public boolean isExsist(String key){
		return redisTemplate.hasKey(key);
	}
}
