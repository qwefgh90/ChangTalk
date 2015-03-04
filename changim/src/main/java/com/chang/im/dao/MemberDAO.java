package com.chang.im.dao;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.chang.im.dto.Member;

@Repository
public class MemberDAO extends BaseDAO {
	@Autowired
	public RedisTemplate<String, Member> redisTemplateForMember;
	
	@Resource(name="redisTemplateForMember")
    private ValueOperations<String, Member> valueOps;
	
	public void registerMember(Member member){
		String key = key(member.getId());
		valueOps.set(key, member);
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
