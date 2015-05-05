package com.chang.im.service.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
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
import com.chang.im.dto.LoginInfo;
import com.chang.im.dto.Member;
import com.chang.im.dto.TokenListItem;
import com.chang.im.service.MemberService;
import com.chang.im.util.IMUtil;

/**
 * 
 * @author cheochangwon
 * 회원가입/ 로그인/ 토큰/ 사용자 정보/ 토큰 갱신 테스트
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@Transactional
public class ServiceTest {

	@Autowired
	MemberService memberService;

	final Member member = new Member();
	final LoginInfo userInfo = new LoginInfo();
	final TokenListItem item = new TokenListItem();

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
	public void memberServiceTest() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		notExistsUserBefore();
		registerUser();
		getAllID();
		getMember();
		login();
		setToken();
		updateTokenDate();
		assertTrue(memberService.logout(userInfo.getToken()));
		existsUser();
		deleteUser();
		notExistsUser();
	}

	private CountDownLatch lock = new CountDownLatch(1);

	/**
	 * 회원가입 / 로그인 / 토큰 테스트
	 * @throws InterruptedException
	 */
	@Test
	public void expireTest() throws InterruptedException{
		MemberService.timeoutSecond = 5;
		registerUser();
		login();
		TokenListItem item = memberService.getTokenListItem(member.getId());
		String token = item.getToken();
		LoginInfo info = memberService.getUserInfo(token);
		assertNotNull(item);
		assertNotNull(info);

		lock.await(5000, TimeUnit.MILLISECONDS);

		item = memberService.getTokenListItem(member.getId());
		info = memberService.getUserInfo(token);
		assertNull(item);
		assertNull(info);
	}

	/**
	 * 토큰 갱신 테스트
	 * 유효시간 5초로 설정
	 * @throws InterruptedException
	 */
	@Test
	public void expireTest2() throws InterruptedException{
		MemberService.timeoutSecond = 5;
		registerUser();
		login();
		TokenListItem item = memberService.getTokenListItem(member.getId());
		String token = item.getToken();
		LoginInfo info = memberService.getUserInfo(token);
		assertNotNull(item);
		assertNotNull(info);

		lock.await(3000, TimeUnit.MILLISECONDS);

		memberService.updateTokenDate(token);

		lock.await(3000, TimeUnit.MILLISECONDS);

		item = memberService.getTokenListItem(member.getId());
		info = memberService.getUserInfo(token);
		assertNotNull(item);
		assertNotNull(info);

		lock.await(2000, TimeUnit.MILLISECONDS);

		item = memberService.getTokenListItem(member.getId());
		info = memberService.getUserInfo(token);
		assertNull(item);
		assertNull(info);
	}

	private void updateTokenDate(){
		long before = item.getExpire();
		memberService.updateTokenDate(item.getToken());
		long after = memberService.getTokenListItem(member.getId()).getExpire();
		assertTrue(before != after);
	}

	private void setToken() {
		TokenListItem item = memberService.getTokenListItem(member.getId());
		this.item.setExpire(item.getExpire());
		this.item.setId(item.getId());
		this.item.setToken(item.getToken());
		assertNotNull(item);
		assertNotNull(item.getToken());
		userInfo.setToken(item.getToken());
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

	@Deprecated
	private void getAllID() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		Set<String> idList = memberService.getAllID();
		Iterator<String> iter = idList.iterator();
		Member parameterMember = new Member();
		JSONArray arr = new JSONArray();
		while(iter.hasNext()){
			String id = iter.next();
			parameterMember.setId(id);

			Member dbMember = memberService.getMember(parameterMember);
			dbMember.setPassword(null);
			dbMember.setRoles(null);

			JSONObject object = new JSONObject(IMUtil.objectToMap(dbMember));
			object.remove("password");
			object.remove("roles");
			object.remove("class");

			arr.put(object);
		}
		assertTrue(idList.size() > 0);
		assertTrue(arr.length() > 0);
		String result =  "{\"result\":true,\"list\":"+arr.toString()+"}";
		System.out.println(result);
	}


}
