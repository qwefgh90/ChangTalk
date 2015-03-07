package com.chang.im.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.chang.im.dto.Packet;

@Repository
public class FailDAO extends BaseDAO {
	@Autowired
	RedisTemplate<String,String> redisTemplateForFail;

	@Resource(name="redisTemplateForFail")
	HashOperations<String, String, Packet> hashOps;

	/**
	 * 실패 저장
	 * @param userId
	 * @param packet
	 * @return
	 */
	public boolean saveFailMessage(String userId, Packet packet){
		hashOps.put(key(userId,packet.getRoomId()), packet.getMessageIndex(), packet);
		return true;
	}

	/**
	 * 실패 제거
	 * @param userId
	 * @param roomId
	 * @param messageIndex
	 * @return
	 */
	public boolean deleteFailMessage(String userId, String roomId, String messageIndex){
		if(hashOps.hasKey(key(userId,roomId), messageIndex))
			hashOps.delete(key(userId,roomId), messageIndex);
		return true;
	}
	
	/**
	 * 목록 비우기
	 * @param userId
	 * @param roomId
	 * @return
	 */
	public boolean clearFailMessage(String userId, String roomId){
		delete(key(userId,roomId));
		return true;
	}
	
	/**
	 * 실패 목록
	 * @param userId
	 * @param roomId
	 * @return
	 */
	public List<Packet> getFailMessage(String userId, String roomId){
		Map<String,Packet> map = hashOps.entries(key(userId, roomId));
		if(map == null || map.size() == 0)
			return null;
		List<Packet> result = new ArrayList<Packet>();
		result.addAll(map.values());
		return result;
	}

	private String key(String userId, String roomId){
		return String.format("User:%s:%s:fail", userId,roomId);
	}
}
