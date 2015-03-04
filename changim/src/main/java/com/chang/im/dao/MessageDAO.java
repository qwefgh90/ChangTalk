package com.chang.im.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.chang.im.dto.Packet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Here is publisher
 * @author cheochangwon
 *
 */
@Repository
public class MessageDAO {
	@Autowired
	RedisTemplate<String,String> redisTemplateForMessage;
	
	ObjectMapper mapper = new ObjectMapper();
	
	public boolean sendMessage(String channel, Packet message){
		if(message == null)
			return false;
		try {
			redisTemplateForMessage.convertAndSend(channel, mapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
