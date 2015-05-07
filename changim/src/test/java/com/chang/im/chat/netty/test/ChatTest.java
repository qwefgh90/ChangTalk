package com.chang.im.chat.netty.test;

import static org.junit.Assert.assertTrue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.socket.SocketIO;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.chang.im.chat.netty.JsonAndAuthDecoder;
import com.chang.im.chat.netty.JsonHandler;
import com.chang.im.config.Application;
import com.chang.im.dao.FailDAO;
import com.chang.im.dao.MessageDAO;
import com.chang.im.dto.Member;
import com.chang.im.dto.TokenListItem;
import com.chang.im.service.MemberService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ChatTest {

	String host = "localhost";
	int port = 9090;
	String id = "qwefgh90";
	String password = "password";
	String token;
	SocketIO socket;

	ObjectMapper mapper = new ObjectMapper();

	/** Countdown latch */
	private CountDownLatch lock = new CountDownLatch(1);

	@Autowired
	MemberService memberService;

	@Autowired
	MessageDAO messageDAO;

	@Autowired
	FailDAO failDAO;

	@Before
	public void setup(){
		createNewMember();
		login();
	}

	private void login(){
		Member member= new Member();
		member.setId(id);
		member.setPassword(password);
		memberService.login(member);
		TokenListItem item = memberService.getTokenListItem(id);
		token = item.getToken();
	}

	private void createNewMember(){
		Member member = new Member();
		member.setId(id);
		member.setPassword(password);
		member.setPhone("01073144993");
		member.setRoles(Member.MEMBER_ROLE);
		memberService.registerMember(member);
		//		member.setId("test");
		//		member.setPassword("password");
		//		member.setPhone("01073144993");
		//		member.setRoles(Member.MEMBER_ROLE);
		//		memberService.registerMember(member);
	}

	@Autowired
	StringDecoder stringDecoder;

	@Autowired
	StringEncoder stringEncoder;

	@Autowired
	JsonAndAuthDecoder jsonAndAuthDecoder;

	@Autowired
	JsonHandler jsonHandler;

	@Test
	public void dicard() throws JSONException, InterruptedException{
		JSONObject object = new JSONObject();
		object.put("type", "createRoom");
		object.put("token", token);
		JSONArray friendList  = new JSONArray();
		friendList.put("unknown");
		JSONObject friendObject = new JSONObject();
		friendObject.put("idList", friendList);
		object.put("content", friendObject);

		String m = object.toString();
		EmbeddedChannel ch = new EmbeddedChannel(stringDecoder, stringEncoder,jsonAndAuthDecoder, jsonHandler);
		ByteBuf in = Unpooled.wrappedBuffer(m.getBytes());
		ch.writeInbound(in);	

		boolean flag = false;
		while(flag == false){
			ByteBuf r = (ByteBuf) ch.readOutbound();
			if(r!=null &&r.readableBytes() != 0){
				System.out.println("readableData");
				byte[] bytes = new byte[r.readableBytes()];
				int readerIndex = r.readerIndex();
				r.getBytes(readerIndex, bytes);
				String read = new String(bytes);
				System.out.println(read);
			}
			lock.await(4000, TimeUnit.MILLISECONDS);
		}


//		assertTrue(read.length() > 0);
	}
}
