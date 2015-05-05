package com.chang.im.dao;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.chang.im.dto.Member;

@Repository
public class MemberDAO extends BaseDAO {
	@Autowired
	public RedisTemplate<String, Member> redisTemplateForMember;
	@Autowired
	public RedisTemplate<String, String> redisTemplate;
	
	@Resource(name="redisTemplateForMember")
    private ValueOperations<String, Member> valueOps;

	@Resource(name="redisTemplate")
	private SetOperations<String, String> setOps;
	
	/**
	 * 모든 회원 id 반환
	 * 테스트에서만 사용할 것
	 * @return
	 */
	@Deprecated
	public Set<String> getAllID(){
		return setOps.members("UserList");
	}
	
	public void addID(String id){
		setOps.add("UserList", id);
	}
	
	public void removeID(String id){
		setOps.remove("UserList", id);
	}
	
	public void registerMember(Member member){
		String key = key(member.getId());
		valueOps.set(key, member);
		
		setOps.add("UserList", member.getId());
	}

	public Member getMember(String id) {
		// TODO Auto-generated method stub
		String key = key(id);
		return valueOps.get(key);
	}

	public boolean isExistsMember(String id){
		String key = key(id);
		return redisTemplateForMember.hasKey(key);
	}
	
	public void deleteMember(String id) {
		// TODO Auto-generated method stub
		String key = key(id);
		redisTemplateForMember.delete(key);
	}
	
	private String key(String id){
		return "User:"+id;
	}
	
}
