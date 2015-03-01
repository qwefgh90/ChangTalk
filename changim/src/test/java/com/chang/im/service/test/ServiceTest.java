package com.chang.im.service.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.chang.im.config.Application;
import com.chang.im.dto.Member;
import com.chang.im.service.MemberService;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@Transactional
public class ServiceTest {
	
	@Autowired
	MemberService memberService;
	
	final Member member = new Member();
	
	@Before
	public void setup(){
		member.setId("ServiceTestID");
		member.setPassword("ServiceTestPassword");
		member.setPhone("ServiceTestPhone");
		memberService.deleteMember(member);
	}
	
	@After
	public void clean(){
		memberService.deleteMember(member);
	}
	
	@Test 
	@Transactional
	public void memberServiceTest(){
		notExistsUserBefore();
		registerUser();
		getMember();
		login();
		assertTrue(memberService.logout(member.getToken()));
		existsUser();
		deleteUser();
		notExistsUser();
	}
	
	public void notExistsUserBefore(){
		assertFalse(memberService.isExistsMember(member));
	}
	
	public void registerUser(){
		assertTrue(memberService.registerMember(member));
	}

	public void getMember(){
		Member dbMember = memberService.getMember(member);
		assertNotNull(dbMember);
		assertThat(dbMember.getId() , is(member.getId()));
		assertThat(dbMember.getPassword() , is(member.getPassword()));
		assertThat(dbMember.getPhone() , is(member.getPhone()));
	}
	
	public void login(){
		assertTrue(memberService.login(member));
	}
	
	public void existsUser(){
		assertTrue(memberService.isExistsMember(member));
	}
	
	public void deleteUser(){
		assertTrue(memberService.deleteMember(member));
	}
	
	public void notExistsUser(){
		assertFalse(memberService.isExistsMember(member));
	}
	
}
