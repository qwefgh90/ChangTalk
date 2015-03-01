package com.chang.im.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.chang.im.dto.Member;
import com.chang.im.util.IMUtil;

@Repository
public class MemberDAO {
	@Autowired
	public RedisTemplate redisTemplate;
	
	@Resource(name="redisTemplate")
    private ValueOperations<String, Object> valueOps;
	
	@Resource(name="redisTemplate")
    private HashOperations<String, String, Object> hashOps;

	@Resource(name="redisTemplate")
    private SetOperations<String, Object> setOps;
	
	public void registerMember(Member member){
		String key = "User:"+member.getId();
		try {
			hashOps.putAll(key, IMUtil.objectToMap(member));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertUserInfo(Member member){
		String key = "UserInfo:"+member.getToken();
		HashMap map = new HashMap();
		map.put("id", member.getId());
		map.put("phone", member.getPhone());
		hashOps.putAll(key, map);
	}

	public boolean isExistsUserInfo(String token){
		String key = "UserInfo:"+token;
		return redisTemplate.hasKey(key);
	}
	
	public Member getUserInfo(String token){
		Member result = new Member();
		String key = "UserInfo:"+token;
		Map map = hashOps.entries(key);
		try {
			IMUtil.mapToObject(map, result);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result= null;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result= null;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result= null;
		}
		return result;
	}
	
	public void deleteUserInfo(String token){
		String key = "UserInfo:"+token;
		redisTemplate.delete(key);
	}
	
	public void insertTokenList(Member member){
		String key = "TokenList:"+member.getId();
		HashMap map = new HashMap();
		map.put("token", member.getToken());
		map.put("expire", member.getExpire());
		hashOps.putAll(key, map);
	}
	
	public Member getTokenList(Member member){
		Member result = new Member();
		String key = "TokenList:"+member.getId();
		Map map = hashOps.entries(key);
		try {
			IMUtil.mapToObject(map, result);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result= null;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result= null;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result= null;
		}
		return result;
	}

	public boolean isExistsTokenList(Member member){
		String key = "TokenList:"+member.getId();
		return redisTemplate.hasKey(key);
	}
	
	public void deleteTokenList(Member member){
		String key = "TokenList:"+member.getId();
		redisTemplate.delete(key);
	}
	
	public Member getMember(String id) {
		// TODO Auto-generated method stub
		String key = "User:"+id;
		Member member = new Member();
		
		if(redisTemplate.hasKey(key) == false)
			return null;
		
		try {
			Map memberMap = hashOps.entries(key);
			IMUtil.mapToObject(memberMap,member);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			member = null;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			member = null;
		} catch(NullPointerException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			member = null;
		} catch(Exception e){
			e.printStackTrace();
			member = null;
		}
		return member;
	}

	public void deleteMember(String id) {
		// TODO Auto-generated method stub
		String key = "User:"+id;
		redisTemplate.delete(key);
	}
	
}
