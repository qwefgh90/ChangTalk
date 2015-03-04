package com.chang.im.dao;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.chang.im.dto.TokenListItem;

@Repository
public class TokenDAO extends BaseDAO {

	@Autowired
	RedisTemplate<String,TokenListItem> redisTemplateForTokenListItem;
	

	@Resource(name="redisTemplateForTokenListItem")
    private ValueOperations<String, TokenListItem> valueOps;

	public void insertTokenList(TokenListItem item){
		String key = key(item.getId());
		valueOps.set(key, item);
	}
	
	public TokenListItem getTokenList(String id){
		String key = key(id);
		return valueOps.get(key);
	}

	public boolean isExistsTokenList(String id){
		String key = key(id);
		return redisTemplateForTokenListItem.hasKey(key);
	}
	
	public void deleteTokenList(String id){
		String key = key(id);
		redisTemplateForTokenListItem.delete(key);
	}
	
	private String key(String id){
		return "Token:"+id;
	}
	
}
