package com.chang.im.service.test;



import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

import com.chang.im.config.Application;
import com.chang.im.dao.MemberDAO;
import com.chang.im.dto.Member;
import com.chang.im.util.IMUtil;



@RunWith(SpringJUnit4ClassRunner.class)

@SpringApplicationConfiguration(classes = Application.class)

@WebAppConfiguration

public class DAOTest {

	@Autowired

	MemberDAO memberDAO;

	final Member member = new Member();;

	@Before
	public void setup(){
		member.setId("DAOTestID");
		member.setPassword("DAOTestPassword");
		member.setPhone("DAOTestPhone");
		member.setToken("DAOTestToken");
		member.setExpire(IMUtil.getCurrentUnixTime());
		memberDAO.deleteMember(member.getId());
	}
	
	@After
	public void clean(){
		deleteMemberTest();
		memberDAO.deleteUserInfo(member.getToken());
		memberDAO.deleteTokenList(member);
	}
	
	@Test
	public void memberDAOTest(){
		registerMemberTest();
		getMemberTest();
		deleteMemberTest();
	}
	
	@Test
	public void memberDAOTestForToken(){
		memberDAO.insertTokenList(member);
		
		assertTrue(memberDAO.isExistsTokenList(member));
		
		Member result = memberDAO.getTokenList(member);
		
		assertThat(result.getToken(),is(member.getToken()));
		assertThat(result.getExpire(),is(member.getExpire()));
		
		memberDAO.deleteTokenList(member);
		
		assertFalse(memberDAO.isExistsTokenList(member));
		
		memberDAO.insertUserInfo(member);
		assertTrue(memberDAO.isExistsUserInfo(member.getToken()));
		
		result = memberDAO.getUserInfo(member.getToken());
		
		assertThat(result.getId(),is(member.getId()));
		assertThat(result.getPhone(),is(member.getPhone()));
		
		memberDAO.deleteUserInfo(member.getToken());
		assertFalse(memberDAO.isExistsUserInfo(member.getToken()));
	}

	
	
	public void registerMemberTest(){
		memberDAO.registerMember(member);
	}


	public void getMemberTest(){

		Member result = memberDAO.getMember(member.getId());

		assertThat(result.getId(),is(member.getId()));

		assertThat(result.getPassword(),is(member.getPassword()));

		assertThat(result.getPhone(),is(member.getPhone()));

	}



	public void deleteMemberTest(){

		memberDAO.deleteMember(member.getId());

		Member result = memberDAO.getMember(member.getId());

		assertNull(result);

	}

}

