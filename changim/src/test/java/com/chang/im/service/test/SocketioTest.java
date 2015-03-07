package com.chang.im.service.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.chang.im.chat.controller.MessageVerticle;
import com.chang.im.chat.protocol.CreateRoom;
import com.chang.im.chat.protocol.ExitRoom;
import com.chang.im.chat.protocol.Result;
import com.chang.im.chat.protocol.SendMsg;
import com.chang.im.chat.protocol.SendMsgToCli;
import com.chang.im.config.Application;
import com.chang.im.dao.FailDAO;
import com.chang.im.dao.MessageDAO;
import com.chang.im.dto.Member;
import com.chang.im.dto.Packet;
import com.chang.im.dto.TokenListItem;
import com.chang.im.service.MemberService;

/**
 * https://github.com/Gottox/socket.io-java-client
 * socket.io-java-clint로 socketio 테스트
 * @author cheochangwon
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class SocketioTest {

	String host = "localhost";
	int port = 9090;
	String id = "qwefgh90";
	String password = "password";
	String url;
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
		url = "http://" +host+ ":" +port;
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
		member.setId("test");
		member.setPassword("password");
		member.setPhone("01073144993");
		member.setRoles(Member.MEMBER_ROLE);
		memberService.registerMember(member);
	}

	@After
	public void clean(){
		memberService.logout(token);
	}
	
	int beforeSize = 0;
	int afterSize = 0;
	String currentRoomId;
	
	boolean checkConnect;
	boolean checkMessage;

	/**
	 * 방 생성 및 메세지 전송 테스트(메세지 에코 테스트)
	 * @throws InterruptedException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@Test
	public void makeRoom() throws InterruptedException, JsonGenerationException, JsonMappingException, IOException{
		socket = new SocketIO(url);
		socket.addHeader("token", token);
		socket.connect(new IOCallback() {
			@Override
			public void on(String arg0, IOAcknowledge arg1, Object... json) {
				System.out.println("[on]" + arg0 + ", " + json[0].toString());
				if(arg0.equals(MessageVerticle.Protocol.sendMsgToCli.name())){
					SendMsgToCli response= null;
					try {
						response = mapper.readValue((String)json[0], SendMsgToCli.class);
						if(response.getContent().equals("안녕하세요?") == false){
							currentRoomId = response.getRoomId();
							List<Packet> list = failDAO.getFailMessage(id, currentRoomId);
							if(list != null)
								beforeSize = list.size();
							
							//성공적으로 수신했다는 응답
							Result result = new Result();
							result.setResult(true);
							result.setRoomId(response.getRoomId());
							result.setMessageIndex(response.getMessageIndex());
							socket.emit(MessageVerticle.Protocol.sendMsgToCli.name(), mapper.writeValueAsString(result));
							
							//새로운 메세지 전송
							SendMsg msg = new SendMsg();
							msg.setContent("안녕하세요?");
							msg.setRoomId(response.getRoomId());
							socket.emit(MessageVerticle.Protocol.sendMsg.name(), mapper.writeValueAsString(msg));

						}else{
							
							//성공적으로 수신했다는 응답
							Result result = new Result();
							result.setResult(true);
							result.setRoomId(response.getRoomId());
							result.setMessageIndex(response.getMessageIndex());
							socket.emit(MessageVerticle.Protocol.sendMsgToCli.name(), mapper.writeValueAsString(result));
							
							//방을 나간다.
							assertTrue(response.getFrom().equals(id));
							ExitRoom exit = new ExitRoom();
							exit.setRoomId(response.getRoomId());
							socket.emit(MessageVerticle.Protocol.exitRoom.name(), mapper.writeValueAsString(exit));
							return;
						}
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					assertNotNull(response);
					assertTrue(response.getFrom().equals(id));
					checkMessage = true;
				}else if(arg0.equals(MessageVerticle.Protocol.exitRoom.name())){
					try {
						String data = (String)json[0];
						Result result = mapper.readValue(data, Result.class);
						assertTrue(result.isResult());
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					

					List<Packet> list = failDAO.getFailMessage(id, currentRoomId);
					if(list != null)
						afterSize = list.size();
					
					assertTrue(afterSize+1 == beforeSize);
					lock.countDown();
				}
			}
			@Override
			public void onConnect() {
				System.out.println("[onConnect]");
				//				lock.countDown();
			}
			@Override
			public void onDisconnect() {
				System.out.println("[onDisconnect]");
				//				lock.countDown();
			}
			@Override
			public void onError(SocketIOException arg0) {
				System.out.println("[onError]"+arg0);
				//				lock.countDown();
			}
			@Override
			public void onMessage(String arg0, IOAcknowledge arg1) {
				System.out.println("[onMessage]"+arg0);
			}
			@Override
			public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
				System.out.println("[onMessage]" + arg0);
			}
		});
		CreateRoom room = new CreateRoom();
		List list = new ArrayList();
		list.add("test");
		room.setIdList(list);

		socket.emit(MessageVerticle.Protocol.createRoom.name(), mapper.writeValueAsString(room));

		lock.await(20000,TimeUnit.MILLISECONDS);
		
		socket.disconnect();

		assertTrue(checkMessage);
	}

	@Test
	public void connection() throws MalformedURLException, InterruptedException{
		socket = new SocketIO(url);
		socket.addHeader("token", token);
		socket.connect(new IOCallback() {

			@Override
			public void on(String arg0, IOAcknowledge arg1, Object... arg2) {
				System.out.println("[on]"+arg0 +", "+arg1 +", "+arg2);
				//				lock.countDown();
			}
			@Override
			public void onConnect() {
				System.out.println("[onConnect]");
				checkConnect = true;
				//				lock.countDown();
			}
			@Override
			public void onDisconnect() {
				System.out.println("[onDisconnect]");
				//				lock.countDown();
			}
			@Override
			public void onError(SocketIOException arg0) {
				System.out.println("[onError]"+arg0);
				//				lock.countDown();
			}
			@Override
			public void onMessage(String arg0, IOAcknowledge arg1) {
				System.out.println("[onMessage]"+arg0);
				//				lock.countDown();
			}
			@Override
			public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
				System.out.println("[onMessage]" + arg0);
				//				lock.countDown();
			}
		});
		lock.await(3000,TimeUnit.MILLISECONDS);
		//onConnect
		assertTrue(checkConnect);

	}
}
