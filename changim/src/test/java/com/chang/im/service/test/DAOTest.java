package com.chang.im.service.test;



import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
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
import com.chang.im.dao.TokenDAO;
import com.chang.im.dao.LoginInfoDAO;
import com.chang.im.dto.Member;
import com.chang.im.dto.LoginInfo;
import com.chang.im.util.IMUtil;



//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = Application.class)
//@WebAppConfiguration
public class DAOTest {
	@Autowired
	MemberDAO memberDAO;

	@Autowired
	LoginInfoDAO userInfoDAO;

	@Autowired
	TokenDAO tokenDAO;

	final Member member = new Member();;
	final LoginInfo info = new LoginInfo();;

//	@Before
//	public void setup(){
//	}
//	
//	@After
//	public void clean(){
//	}

}

