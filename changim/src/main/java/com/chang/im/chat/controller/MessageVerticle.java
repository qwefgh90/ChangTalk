package com.chang.im.chat.controller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonObject;

import com.chang.im.chat.protocol.CreateRoom;
import com.chang.im.chat.protocol.ExitRoom;
import com.chang.im.chat.protocol.Result;
import com.chang.im.chat.protocol.SendMsg;
import com.chang.im.dao.FailDAO;
import com.chang.im.dao.IndexDAO;
import com.chang.im.dao.MessageDAO;
import com.chang.im.dto.LoginInfo;
import com.chang.im.dto.Member;
import com.chang.im.dto.Packet;
import com.chang.im.service.MemberService;
import com.chang.im.service.MessageListenerImpl;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhncorp.mods.socket.io.SocketIOServer;
import com.nhncorp.mods.socket.io.SocketIOSocket;
import com.nhncorp.mods.socket.io.impl.AuthorizationCallback;
import com.nhncorp.mods.socket.io.impl.AuthorizationHandler;
import com.nhncorp.mods.socket.io.impl.DefaultSocketIOServer;
import com.nhncorp.mods.socket.io.impl.HandshakeData;

/**
 * @author Keesun Baik
 */
@Component
public class MessageVerticle extends DefaultEmbeddableVerticle {

	public static enum Protocol{
		createRoom,sendMsg,sendMsgToCli,exitRoom,reqFail,reqMsg
	}

	private SocketIOServer io;

	@Autowired
	MessageDAO messageDAO;

	@Autowired
	IndexDAO indexDAO;

	@Autowired
	FailDAO failDAO;

	@Autowired
	RedisMessageListenerContainer redisContainer;

	@Autowired
	MemberService memberService;

	@Resource
	ConcurrentMap<String,SocketIOSocket> socketMap;

	@Resource
	ConcurrentHashMap<String, List<MessageListener>> listenerMap;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void start(Vertx vertx) {
		HttpServer server = vertx.createHttpServer();
		io = new DefaultSocketIOServer(vertx, server);
		io.setAuthHandler(new AuthorizationHandler() {
			/**
			 * Security handshaking
			 */
			@Override
			public void handle(HandshakeData handshakeData, AuthorizationCallback callback) {
				String htoken = handshakeData.getHeaders().get("token");
				if(htoken != null && memberService.isExistToken(htoken) == true){
					callback.handle(null, true);
				}else{
					callback.handle(new RuntimeException("Not authorization"), false);
				}
			}
		});

		/**
		 * 연결 시 처리
		 */
		io.sockets().onConnection(new Handler<SocketIOSocket>() {
			public void handle(final SocketIOSocket socket) {

				String htoken = socket.handshakeData().getHeaders().get("token");
				LoginInfo info = memberService.getUserInfo(htoken);
				final String myId = info.getId();

				//리스너 컨테이너 생성
				listenerMap.put(myId, new ArrayList<MessageListener>());

				//소켓 등록
				socketMap.put(myId, socket);
				socket.emit("hello","welcome : "+"Chang im chat is started");

				/**
				 * 소켓 종료  / 자원정리
				 */
				socket.onDisconnect(new Handler<JsonObject>(){
					@Override
					public void handle(JsonObject event) {

						//subscribe 해제
						List<MessageListener> list = listenerMap.get(myId);
						for(MessageListener item : list){
							redisContainer.removeMessageListener(item);
						}
						list.clear();
						listenerMap.remove(myId);
					}
				});

				/**
				 * 채팅방 생성 로직 
				 */
				socket.on(Protocol.createRoom.name(), new Handler<JsonObject>(){
					@Override
					public void handle(JsonObject event) {
						CreateRoom req;
						try{
							String data = event.getString("data");
							req = mapper.readValue(data, CreateRoom.class);
						}catch(Exception e){
							e.printStackTrace();
							socket.emit(Protocol.createRoom.name(), "{\"result\":false}");
							return;
						}
						try{
							//방정보 생성
							String roomId = indexDAO.increaseRoomIndex();	//방 인덱스

							List<String> idList = req.getIdList();

							//방접속 정보 저장
							messageDAO.saveRoomList(myId, roomId);
							messageDAO.saveRoomUser(roomId, myId);
							MessageListener listener = new MessageListenerImpl(socket, myId, roomId, failDAO);
							redisContainer.addMessageListener(listener, new ChannelTopic(roomId));	//리스너 자원 관리
							listenerMap.get(myId).add(listener);

							for(String id : idList){
								Member member = new Member();
								member.setId(id); 
								//존재하는 사용자 일 경우에만
								if(memberService.isExistsMember(member) == true && id.equals(myId) == false ){
									messageDAO.saveRoomList(id, roomId);
									messageDAO.saveRoomUser(roomId, id);

									SocketIOSocket socket;
									if( (socket = socketMap.get(id)) != null){
										MessageListener listenerForSubscriber = new MessageListenerImpl(socket, id, roomId, failDAO);
										redisContainer.addMessageListener(listenerForSubscriber, new ChannelTopic(roomId));	
										listenerMap.get(id).add(listenerForSubscriber);
									}
								}
							}

							//초기 메세지 저장/전송 (publish)

							String messageIndex = indexDAO.increaseMessageIndex(roomId);

							Packet packet = new Packet();
							packet.setFromId(myId);
							packet.setContent(myId+"님의 채팅방에 초대 되었습니다.");
							packet.setRoomId(roomId);
							packet.setTimestamp(System.currentTimeMillis() / 1000);
							packet.setMessageIndex(messageIndex);

							messageDAO.saveMessage(roomId, packet);	//Redis 저장
							messageDAO.sendMessage(roomId, packet);	//메세지 전송 (publish)
							socket.emit(Protocol.createRoom.name(), "{\"result\":true,\"roomId\":\""+roomId+"\"}");
						}catch(Exception e){
							e.printStackTrace();
							socket.emit(Protocol.createRoom.name(), "{\"result\":false}");
							return;
						}
					}
				});

				/**
				 * 메세지 전송 처리
				 */
				socket.on(Protocol.sendMsg.name(), new Handler<JsonObject>(){
					@Override
					public void handle(JsonObject event) {
						try {
							String data = event.getString("data");
							SendMsg msg = mapper.readValue(data, SendMsg.class);

							//유효성 검사
							if(messageDAO.findRoomList(myId, msg.getRoomId()) == true){

								String messageIndex = indexDAO.increaseMessageIndex(msg.getRoomId());

								Packet packet = new Packet();
								packet.setFromId(myId);
								packet.setContent(msg.getContent());
								packet.setRoomId(msg.getRoomId());
								packet.setMessageIndex(messageIndex);
								packet.setTimestamp(System.currentTimeMillis() / 1000);

								//메세지 전송 (publish)
								messageDAO.sendMessage(msg.getRoomId(), packet);
								socket.emit(Protocol.sendMsg.name(), "{\"result\":true}");
							}
						} catch (JsonParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							socket.emit(Protocol.createRoom.name(), "{\"result\":false}");
						} catch (JsonMappingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							socket.emit(Protocol.createRoom.name(), "{\"result\":false}");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							socket.emit(Protocol.createRoom.name(), "{\"result\":false}");
						} catch (Exception e){
							e.printStackTrace();
							socket.emit(Protocol.createRoom.name(), "{\"result\":false}");
						}
					}
				});


				socket.on(Protocol.exitRoom.name(), new Handler<JsonObject>(){
					@Override
					public void handle(JsonObject event) {
						try{
							String data = event.getString("data");
							ExitRoom exitRoom = mapper.readValue(data, ExitRoom.class);
							String roomId = exitRoom.getRoomId();
							if(messageDAO.findRoomList(myId, roomId)){
								messageDAO.deleteRoomList(myId, roomId);
								messageDAO.deleteRoomUser(roomId, myId);
								socket.emit(Protocol.exitRoom.name(), "{\"result\":true}");
							}
						}catch(Exception e){
							e.printStackTrace();
							socket.emit(Protocol.exitRoom.name(), "{\"result\":false}");
							return;
						}
					}
				});

				socket.on(Protocol.reqFail.name(), new Handler<JsonObject>(){
					@Override
					public void handle(JsonObject event) {
						// TODO Auto-generated method stub

					}
				});

				socket.on(Protocol.reqMsg.name(), new Handler<JsonObject>(){
					@Override
					public void handle(JsonObject event) {
						// TODO Auto-generated method stub

					}
				});

				/**
				 * 메세지 전송 성공 처리
				 */
				socket.on(Protocol.sendMsgToCli.name(), new Handler<JsonObject>(){
					@Override
					public void handle(JsonObject event) {
						String data = event.getString("data");
						try {
							Result result =	mapper.readValue(data, Result.class);
							failDAO.deleteFailMessage(myId, result.getRoomId(), result.getMessageIndex());
						} catch (JsonParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JsonMappingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e){
							e.printStackTrace();
						}

					}
				});

				socket.on("echo", new Handler<JsonObject>() {
					public void handle(JsonObject msg) {
						socket.emit("echo","changim : "+ msg);
						Packet p = new Packet();
						p.setContent(msg.toString());
						p.setFromId("id");
						p.setTimestamp(1234L);
						messageDAO.sendMessage("CHANGIM", p);
					}
				});
				/**
				 * 1)인증
				 * - Publish
				 * - io 연결끊김처리
				 * - 
				 */
			}
		});
		server.listen(9090);
	}

	public SocketIOServer getIo() {
		return io;
	}
}