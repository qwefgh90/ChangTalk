package com.chang.im.service;

import io.netty.channel.Channel;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.chang.im.chat.netty.JsonHandler.Protocol;
import com.chang.im.chat.protocol.SendMsgToCli;
import com.chang.im.dao.FailDAO;
import com.chang.im.dto.Packet;
import com.chang.im.util.IMUtil;
/**
 * Here is Subscriber
 * Spring data redis thread
 * @author cheochangwon
 *
 */
@Component
public class MessageListenerImpl implements MessageListener {

	ObjectMapper mapper = new ObjectMapper();

	public MessageListenerImpl(){}

	private static ConcurrentHashMap<String,Channel> idChannelMap;

	@Autowired(required = true)
	public void setIdChannelMap(ConcurrentHashMap<String,Channel> idChannelMap) {
		MessageListenerImpl.idChannelMap = idChannelMap;
	}

	String channel;
	String id;
	FailDAO failDAO;

	//Room Subscriber 정보, 채널, 소켓
	public MessageListenerImpl( String id, String channel, FailDAO failDAO){
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

		JSONObject object = null;
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

			object = new JSONObject();
			object.put("type", Protocol.sendMsgToCli.name());
			object.put("result", true);

			Map map = IMUtil.objectToMap(clientMsg);
			map.remove("class");
			
			JSONObject body = new JSONObject();
			Set<String> keys = map.keySet();
			for(String key : keys){
				body.put(key, map.get(key));
			}
			object.put("response", body);

			//실패 메세지 큐 등록 //응답에서 제거
			failDAO.saveFailMessage(id, packet);

			System.out.println("Redis onMessage");
		} catch (JsonParseException e) {
			e.printStackTrace();

		} catch (JsonMappingException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} catch (Exception e){
			e.printStackTrace();
		}finally{
			//메세지 전송
			Channel ch = idChannelMap.get(id);
			if(null != ch)
				ch.writeAndFlush(object.toString());
		}
	}
}
