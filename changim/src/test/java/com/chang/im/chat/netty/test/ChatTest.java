package com.chang.im.chat.netty.test;

import static org.junit.Assert.*;
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
import com.chang.im.chat.netty.JsonHandler.Protocol;
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
	String roomId;
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
	public void nettyTest() throws JSONException, InterruptedException{
		createRoomTest();	//방생성
		sendMsgTest();		//메세지전송
		failReqTest();		//실패메세지 요청
		reqMsgTest();		//시간 기준 요청
		reqAllIdTest();		//모든 아이디 요청
		exitRoomTest();		//방나가기
	}
	
	/**
	 * 방생성 요청/응답 테스트 
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	public void createRoomTest() throws JSONException, InterruptedException{
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
		int loop = 0;
		while(flag == false){
			ByteBuf r = (ByteBuf) ch.readOutbound();
			if(loop == 3){
				assertTrue(false);
			}
			if(r!=null &&r.readableBytes() != 0){
				byte[] bytes = new byte[r.readableBytes()];
				int readerIndex = r.readerIndex();
				r.getBytes(readerIndex, bytes);
				String read = new String(bytes);
				System.out.println(read);
				JSONObject response = new JSONObject(read);
				if(read.contains("message")){
					if(loop == 1)
						assertTrue(true);
					else
						assertTrue(false);
					break;
				}else{
					roomId = (String)response.getJSONObject("response").get("roomId");
				}
				loop++;
			}
			lock.await(2000, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * 메세지 요청/응답 테스트 
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	public void sendMsgTest() throws JSONException, InterruptedException{
		JSONObject object = new JSONObject();
		object.put("type", "sendMsg");
		object.put("token", token);
		JSONObject msg = new JSONObject();
		msg.put("roomId", roomId);
		msg.put("content", "송신 메세지");
		object.put("content", msg);

		String m = object.toString();
		EmbeddedChannel ch = new EmbeddedChannel(stringDecoder, stringEncoder,jsonAndAuthDecoder, jsonHandler);
		ByteBuf in = Unpooled.wrappedBuffer(m.getBytes());
		ch.writeInbound(in);	

		boolean flag = false;
		int loop = 0;
		while(flag == false){
			ByteBuf r = (ByteBuf) ch.readOutbound();
			if(loop == 3){
				assertTrue(false);
			}
			if(r!=null &&r.readableBytes() != 0){
				byte[] bytes = new byte[r.readableBytes()];
				int readerIndex = r.readerIndex();
				r.getBytes(readerIndex, bytes);
				String read = new String(bytes);
				System.out.println(read);
				if(read.contains("response")){	//메세지 수신
					if(loop == 1){
						JSONObject response = new JSONObject(read);
						//ack 메세지 전송
						JSONObject ack = new JSONObject();
						ack.put("token", token);
						ack.put("type", Protocol.sendMsgToCli.name());
						JSONObject content = new JSONObject();
						content.put("roomId", roomId);
						content.put("messageIndex", response.getJSONObject("response").get("messageIndex"));
						ack.put("content", content);
						
						ByteBuf ackbyte = Unpooled.wrappedBuffer(ack.toString().getBytes());
						ch.writeInbound(ackbyte);
						assertTrue(true);
					}
					else
						assertTrue(false);
					break;
				}
				
				loop++;
			}
			lock.await(2000, TimeUnit.MILLISECONDS);
		}
		lock.await(1000, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 실패메세지 요청 테스트 
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	public void failReqTest() throws JSONException, InterruptedException{
		JSONObject object = new JSONObject();
		object.put("type", "reqFail");
		object.put("token", token);
		JSONObject msg = new JSONObject();
		msg.put("roomId", roomId);
		object.put("content", msg);

		String m = object.toString();
		EmbeddedChannel ch = new EmbeddedChannel(stringDecoder, stringEncoder,jsonAndAuthDecoder, jsonHandler);
		ByteBuf in = Unpooled.wrappedBuffer(m.getBytes());
		ch.writeInbound(in);	

		boolean flag = false;
		int loop = 0;
		while(flag == false){
			ByteBuf r = (ByteBuf) ch.readOutbound();
			if(loop == 1){
				assertTrue(false);
			}
			if(r!=null &&r.readableBytes() != 0){
				byte[] bytes = new byte[r.readableBytes()];
				int readerIndex = r.readerIndex();
				r.getBytes(readerIndex, bytes);
				String read = new String(bytes);
				System.out.println(read);
				if(read.contains("response")){
					if(loop == 0)
						assertTrue(true);
					else
						assertTrue(false);
					break;
				}
				loop++;
			}
			lock.await(2000, TimeUnit.MILLISECONDS);
		}
	}
	/**
	 * 메세지 요청 테스트 
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	public void reqMsgTest() throws JSONException, InterruptedException{
		JSONObject object = new JSONObject();
		object.put("type", "reqMsg");
		object.put("token", token);
		JSONObject msg = new JSONObject();
		msg.put("roomId", roomId);
		msg.put("lastTime", "0");
		object.put("content", msg);

		String m = object.toString();
		EmbeddedChannel ch = new EmbeddedChannel(stringDecoder, stringEncoder,jsonAndAuthDecoder, jsonHandler);
		ByteBuf in = Unpooled.wrappedBuffer(m.getBytes());
		ch.writeInbound(in);	

		boolean flag = false;
		int loop = 0;
		while(flag == false){
			ByteBuf r = (ByteBuf) ch.readOutbound();
			if(loop == 1){
				assertTrue(false);
			}
			if(r!=null &&r.readableBytes() != 0){
				byte[] bytes = new byte[r.readableBytes()];
				int readerIndex = r.readerIndex();
				r.getBytes(readerIndex, bytes);
				String read = new String(bytes);
				System.out.println(read);
				if(read.contains("response")){
					if(loop == 0)
						assertTrue(true);
					else
						assertTrue(false);
					break;
				}
				loop++;
			}
			lock.await(2000, TimeUnit.MILLISECONDS);
		}
	}
	/**
	 * 모든 아이디 요청 테스트
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	public void reqAllIdTest() throws JSONException, InterruptedException{
		JSONObject object = new JSONObject();
		object.put("type", "reqAllID");
		object.put("token", token);
		JSONObject msg = new JSONObject();

		String m = object.toString();
		EmbeddedChannel ch = new EmbeddedChannel(stringDecoder, stringEncoder,jsonAndAuthDecoder, jsonHandler);
		ByteBuf in = Unpooled.wrappedBuffer(m.getBytes());
		ch.writeInbound(in);	

		boolean flag = false;
		int loop = 0;
		while(flag == false){
			ByteBuf r = (ByteBuf) ch.readOutbound();
			if(loop == 1){
				assertTrue(false);
			}
			if(r!=null &&r.readableBytes() != 0){
				byte[] bytes = new byte[r.readableBytes()];
				int readerIndex = r.readerIndex();
				r.getBytes(readerIndex, bytes);
				String read = new String(bytes);
				System.out.println(read);
				if(read.contains("response")){
					if(loop == 0)
						assertTrue(true);
					else
						assertTrue(false);
					break;
				}
				loop++;
			}
			lock.await(2000, TimeUnit.MILLISECONDS);
		}
	}
	/**
	 * 방나가기 테스트
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	public void exitRoomTest() throws JSONException, InterruptedException{
		JSONObject object = new JSONObject();
		object.put("type", "exitRoom");
		object.put("token", token);
		JSONObject msg = new JSONObject();
		msg.put("roomId", roomId);
		object.put("content", msg);
		
		String m = object.toString();
		EmbeddedChannel ch = new EmbeddedChannel(stringDecoder, stringEncoder,jsonAndAuthDecoder, jsonHandler);
		ByteBuf in = Unpooled.wrappedBuffer(m.getBytes());
		ch.writeInbound(in);	

		boolean flag = false;
		int loop = 0;
		while(flag == false){
			ByteBuf r = (ByteBuf) ch.readOutbound();
			if(loop == 1){
				assertTrue(false);
			}
			if(r!=null &&r.readableBytes() != 0){
				byte[] bytes = new byte[r.readableBytes()];
				int readerIndex = r.readerIndex();
				r.getBytes(readerIndex, bytes);
				String read = new String(bytes);
				System.out.println(read);
				if(read.contains("exitRoom")){
					if(loop == 0)
						assertTrue(true);
					else
						assertTrue(false);
					break;
				}
				loop++;
			}
			lock.await(3000, TimeUnit.MILLISECONDS);
		}
	}
}
