package com.chang.im.service;

import io.netty.channel.Channel;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import com.chang.im.chat.controller.MessageVerticle;
import com.chang.im.chat.protocol.SendMsgToCli;
import com.chang.im.dao.FailDAO;
import com.chang.im.dto.Packet;
import com.nhncorp.mods.socket.io.SocketIOSocket;
/**
 * Here is Subscriber
 * Spring data redis thread
 * @author cheochangwon
 *
 */
public class MessageListenerImpl implements MessageListener {
	
	ObjectMapper mapper = new ObjectMapper();

	public MessageListenerImpl(){}
	
	String channel;
	String id;
	Channel socketChannel;
	
	FailDAO failDAO;

	//Room Subscriber 정보, 채널, 소켓
	public MessageListenerImpl(Channel socketChannel, String id, String channel, FailDAO failDAO){
		this.socketChannel = socketChannel;
		this.id = id;
		this.channel = channel;
		this.failDAO = failDAO;
	}
	/**
	 * 구독한 모든 메세지 처리하는 로직
	 * 1)Redis에 메세지 저장
	 * 2)SocketIO를 통해 클라이언트에 전송
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {

		try {
			Packet packet = mapper.readValue(message.getBody(), Packet.class);
//			System.out.println("[MessageListenerImpl] "
//					+packet.getFromId()+", "
//					+packet.getContent()+", "
//					+packet.getTimestamp());
			
			//메세지 생성
			SendMsgToCli clientMsg = new SendMsgToCli();
			clientMsg.setContent(packet.getContent());
			clientMsg.setFrom(packet.getFromId());
			clientMsg.setMessageIndex(packet.getMessageIndex());
			clientMsg.setTimestamp(packet.getTimestamp());
			clientMsg.setRoomId(packet.getRoomId());

			//실패 메세지 큐 등록 //응답에서 제거
			failDAO.saveFailMessage(id, packet);
			
			System.out.println("Redis onMessage");
			//메세지 전송 
			socketChannel.writeAndFlush(clientMsg.json().toString());
		} catch (JsonParseException e) {
			e.printStackTrace();
		
		} catch (JsonMappingException e) {
			e.printStackTrace();
		
		} catch (IOException e) {
			e.printStackTrace();
		
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
