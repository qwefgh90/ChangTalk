package com.chang.im.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
public class IndexDAO extends BaseDAO{
	
	@Autowired
	RedisTemplate<String,String> redisTemplate;
	
	private final String roomKey = "RoomIndex"; 
	
	
	private String messageKey(String roomId){
		return String.format("RoomIndex:%s:MessageIndex", roomId);
	}
	/**
	 * 해당방의 다음 메세지 인덱스
	 * @param roomId
	 * @return
	 */
	public String increaseMessageIndex(final String roomId){
		List<Object> result = redisTemplate.execute(new SessionCallback<List<Object>>() {
		    public List<Object> execute(RedisOperations operations) throws DataAccessException {
		    	final String key = messageKey(roomId);
		    	boolean has = operations.hasKey(key);	//트랜잭션 안에선 가져올 수 없음
		    	ValueOperations<String,String> valueOps = operations.opsForValue();
		    	
		    	operations.multi();
		    	if(has){
			    	valueOps.increment(key, 1);	
		    	}else{
		    		valueOps.set(key, "0");
		    	}
		    	// This will contain the results of all ops in the transaction
		    	List<Object> result = operations.exec();
		    	if(result.size() == 0)
		    		result.add("0");
		        return result;
		    }
		});
		
		return (String)result.get(0).toString();
	}
	
	/**
	 * 방의 다음 인덱스
	 * @return
	 */
	public String increaseRoomIndex(){
		List<Object> result = redisTemplate.execute(new SessionCallback<List<Object>>() {
		    public List<Object> execute(RedisOperations operations) throws DataAccessException {
		    	final String key = roomKey;
		    	boolean has = operations.hasKey(key);	//트랜잭션 안에선 가져올 수 없음
		    	ValueOperations<String,String> valueOps = operations.opsForValue();
		    	
		    	operations.multi();
		    	if(has){
			    	valueOps.increment(key, 1);	
		    	}else{
		    		valueOps.set(key, "0");
		    	}
		    	// This will contain the results of all ops in the transaction
		    	List<Object> result = operations.exec();
		    	if(result.size() == 0)
		    		result.add("0");
		        return result;
		    }
		});
		
		return (String)result.get(0).toString();
	}

	/**
	 * 현재 메세지 인덱스
	 * @param roomId
	 * @return
	 */
	public String currentMessageIndex(final String roomId){
		final String key = messageKey(roomId);
		return super.getValue(key);
	}
	
	/**
	 * 현재 방 인덱스
	 * @return
	 */
	public String currentRoomIndex(){
		final String key = roomKey;
		return super.getValue(key);
	}
	
	/**
	 * 메세지 인덱스 삭제
	 * @param roomId
	 */
	public void deleteMessageIndex(final String roomId){
		final String key = messageKey(roomId);
		delete(key);
	}
	
	/**
	 * 방 인덱스 삭제
	 * @param roomId
	 */
	public void deleteRoomIndex(){
		final String key = roomKey;
		delete(key);
	}

	
}
