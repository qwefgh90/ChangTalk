package com.chang.im.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonObject;

import com.chang.im.dao.MessageDAO;
import com.chang.im.dto.Packet;
import com.chang.im.service.MemberService;
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
public class SampleVerticle extends DefaultEmbeddableVerticle {

	private SocketIOServer io;

	@Autowired
	MessageDAO messageDAO;
	
	@Autowired
	RedisMessageListenerContainer redisContainer;

	@Autowired
	MemberService memberService;
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void start(Vertx vertx) {
		HttpServer server = vertx.createHttpServer();
		io = new DefaultSocketIOServer(vertx, server);
		io.setAuthHandler(new AuthorizationHandler() {
			@Override
			public void handle(HandshakeData handshakeData, AuthorizationCallback callback) {
//				String token = handshakeData.getQueryParams().get("pass");
				String htoken = handshakeData.getHeaders().get("token");
				if(htoken != null && htoken.equals("true")){//memberService.isExistToken(token) == true){
					callback.handle(null, true);
				}else{
					callback.handle(new RuntimeException("Not authorization"), false);
				}
			}
		});
		io.sockets().onConnection(new Handler<SocketIOSocket>() {
			public void handle(final SocketIOSocket socket) {
				socket.emit("welcome : "+"Chang im chat is started");
				socket.on("echo", new Handler<JsonObject>() {
					public void handle(JsonObject msg) {
						socket.emit("echo","changim : "+ msg);
						Packet p = new Packet();
						p.setContent(msg.toString());
						p.setFromID("id");
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