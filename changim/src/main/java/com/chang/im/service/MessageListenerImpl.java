package com.chang.im.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import com.chang.im.dto.Packet;
/**
 * Here is Subscriber
 * Spring data redis thread
 * @author cheochangwon
 *
 */
public class MessageListenerImpl implements MessageListener {

	ObjectMapper mapper = new ObjectMapper();

	public MessageListenerImpl(){}

	//Room Subscriber 정보, 채널, 소켓, 
	public MessageListenerImpl(Object socket){

	}
	/**
	 * 구독한 모든 메세지 처리하는 로직
	 * 1)Redis에 메세지 저장
	 * 2)SocketIO를 통해 클라이언트에 전송
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			System.out.println("[MessageListenerImpl] channel: "
					+new String(message.getChannel(),"UTF-8")
					+", body: "+new String(message.getBody(),"UTF-8")
					+"\n pattern: "+new String(pattern,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			Packet packet = mapper.readValue(message.getBody(), Packet.class);
			System.out.println("[MessageListenerImpl] "
					+packet.getFromID()+", "
					+packet.getContent()+", "
					+packet.getTimestamp());
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
	}
}
