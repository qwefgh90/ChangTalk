package com.chang.im.dao;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
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
public class MessageDAO extends BaseDAO {
	@Autowired
	RedisTemplate<String,String> redisTemplateForMessage;

	@Resource(name="redisTemplateForMessage")
	public SetOperations<String, String> setOps;

	@Resource(name="redisTemplateForMessage")
	public ZSetOperations<String, String> zsetOps;

	ObjectMapper mapper = new ObjectMapper();

	/**
	 * redis publish 사용 
	 * @param channel
	 * @param packet
	 * @return
	 */
	public boolean sendMessage(String channel, Packet packet){
		if(packet == null)
			return false;
		String json;
		try {
			json = mapper.writeValueAsString(packet);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return false;
		}
		redisTemplateForMessage.convertAndSend(channel, json);
		
		
		return true;
	}

	/**
	 * 사용자 리스트 저장
	 * @param roomId
	 * @param userId
	 * @return
	 */
	public boolean saveRoomUser(String roomId, String userId){
		Long returnValue = setOps.add(roomUserKey(roomId), userId);
		return true;
	}

	public boolean deleteRoomUser(String roomId, String userId){
		Long returnValue = setOps.remove(roomUserKey(roomId), userId);
		return true;
	}
	
	public boolean findRoomUser(String roomId, String userId){
		return setOps.isMember(roomUserKey(roomId), userId);
	}
	
	/**
	 * 톡 리스트 저장
	 * @param userId
	 * @param roomId
	 * @return
	 */
	public boolean saveRoomList(String userId, String roomId){
		Long returnValue = setOps.add(userRoomKey(userId), roomId);
		return true;
	}

	public boolean deleteRoomList(String userId, String roomId){
		Long returnValue = setOps.remove(userRoomKey(userId), roomId);
		return true;
	}
	
	public boolean findRoomList(String userId, String roomId){
		return setOps.isMember(userRoomKey(userId), roomId);
	}
	
	/**
	 * 메세지 저장 (Publish만 저장하고 시간으로 가져올 수 있음)
	 * @param roomId
	 * @param packet
	 * @return
	 */
	public boolean saveMessage(String roomId, Packet packet){
		String json;
		try {
			json = mapper.writeValueAsString(packet);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		zsetOps.add(messageKey(roomId), json, packet.getTimestamp());
		return true;
	}
	
	public void deleteMessages(String roomId, Long startTime, Long endTime){
		zsetOps.removeRangeByScore(roomId, startTime, endTime);
	}
	
	public void deleteMessages(String roomId, Long endTime){
		zsetOps.removeRangeByScore(messageKey(roomId), -1, endTime);
	}
	
	public boolean readMessage(String roomId, Long startTime, Long endTime){
		zsetOps.rangeByScore(messageKey(roomId), startTime, endTime);
		return true;
	}
	
	public Set<String> readMessage(String roomId, Long startTime){
		Set<String> result = zsetOps.rangeByScore(messageKey(roomId), startTime, Long.MAX_VALUE);
		return result;
	}
	
	private String roomUserKey(String roomId){
		return "RoomUser:"+roomId;
	}

	private String userRoomKey(String userId){
		return "RoomList:"+userId;
	}

	private String messageKey(String roomId){
		return "MessageBox:"+roomId;
	}
}
