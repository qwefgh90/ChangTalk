package com.chang.im.chat.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.chang.im.chat.controller.MessageVerticle.Protocol;
import com.chang.im.chat.protocol.CreateRoom;
import com.chang.im.chat.protocol.ExitRoom;
import com.chang.im.chat.protocol.ReqFail;
import com.chang.im.chat.protocol.ReqMsg;
import com.chang.im.chat.protocol.Result;
import com.chang.im.chat.protocol.SendMsg;
import com.chang.im.chat.protocol.SendMsgToCli;
import com.chang.im.dao.FailDAO;
import com.chang.im.dao.IndexDAO;
import com.chang.im.dao.MessageDAO;
import com.chang.im.dto.Member;
import com.chang.im.dto.Packet;
import com.chang.im.dto.TokenListItem;
import com.chang.im.service.MemberService;
import com.chang.im.service.MessageListenerImpl;
import com.chang.im.util.IMUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Qualifier("jsonHandler")
@Sharable
//공유되는 핸들러라는 사실을 네티에게 알려주는 어노테이션
public class JsonHandler extends SimpleChannelInboundHandler<JSONObject> {
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
	ConcurrentHashMap<String,Channel> tokenChannelMap;

	@Resource
	ConcurrentHashMap<String, List<MessageListener>> listenerMap;

	@Resource
	ConcurrentHashMap<Channel, String> channelIdMap;

	final ObjectMapper mapper = new ObjectMapper();	//thread safe 

	public static enum Protocol{
		createRoom,sendMsg,sendMsgToCli,exitRoom,reqFail,reqMsg,reqAllID
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, JSONObject msg)
			throws Exception {
		String typeStr = (String)msg.get("type");
		String myId = channelIdMap.get(ctx.channel());
		Protocol type = Protocol.valueOf(typeStr);
		switch(type){
		case createRoom:{
			CreateRoom req = null;
			req = (CreateRoom)mapper.readValue(msg.get("content").toString(), CreateRoom.class);
			createRoom(req, myId, ctx);
			break;
		}
		case sendMsg:{
			SendMsg req = null;
			req = (SendMsg)mapper.readValue(msg.get("content").toString(), SendMsg.class);
			sendMsg(req,myId,ctx);
			break;
		}
		case sendMsgToCli:{
			Result result = null;
			result = (Result)mapper.readValue(msg.get("content").toString(), Result.class);
			sendMsgToCli(result,myId,ctx);
			break;
		}
		case exitRoom:{
			ExitRoom req = null;
			req = (ExitRoom)mapper.readValue(msg.get("content").toString(), ExitRoom.class);
			exitRoom(req,myId,ctx);
			break;
		}
		case reqFail:{
			ReqFail req = null;
			req = (ReqFail)mapper.readValue(msg.get("content").toString(), ReqFail.class);
			reqFail(req,myId,ctx);
			break;
		}
		case reqMsg:{
			ReqMsg req = null;
			req = (ReqMsg)mapper.readValue(msg.get("content").toString(), ReqMsg.class);
			reqMsg(req,myId,ctx);
			break;
		}
		case reqAllID:
			reqAllID(myId,ctx);
			break;
		}
		System.out.println(type.name());
	}
	
	/**
	 * 모든 사용자의 정보 요청
	 * 많은 오버헤드
	 */
	@Deprecated
	public void reqAllID(String myId, ChannelHandlerContext ctx){
		Channel socket = ctx.channel();
		// TODO Auto-generated method stub
		try{
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
			socket.writeAndFlush("{\"result\":true,\"list\":"+arr.toString()+"}");
		}catch(Exception e){
			e.printStackTrace();
			socket.writeAndFlush( "{\"result\":false}");
		}
	}

	/**
	 * 메세지에 대한 클라이언트의 확인 응답
	 * 실패 메세지 수신에 대한 확인 응답
	 */
	public void sendMsgToCli(Result result, String myId, ChannelHandlerContext ctx){
		try {
			failDAO.deleteFailMessage(myId, result.getRoomId(), result.getMessageIndex());
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 특정 시간 기준으로 메세지 요청
	 * 별도의 응답이 필요 없음
	 */
	public void reqMsg(ReqMsg reqMsg, String myId, ChannelHandlerContext ctx){
		Channel socket = ctx.channel();
		try{
			//유효성 검사
			if(messageDAO.findRoomList(myId, reqMsg.getRoomId()) == true){

				//시간 기준으로 메세지 읽어옴
				Set<String> messageSet = messageDAO.readMessage(reqMsg.getRoomId(), reqMsg.getLastTime());
				List<Packet> packetList = new ArrayList<Packet>();
				for(String messageStr : messageSet){
					Packet packet = mapper.readValue(messageStr, Packet.class);
					packetList.add(packet);
				}

				Result result = new Result();
				result.setPacket(packetList);
				result.setResult(true);

				String resultStr = mapper.writeValueAsString(result);
				socket.writeAndFlush( resultStr);
			}
		}catch(Exception e){
			e.printStackTrace();
			socket.writeAndFlush("{\"result\":false}");
		}
	}
	
	/**
	 * 실패한 메세지 리스트 요청
	 * Protocol.sendMsgToCli.name() 채널로 응답 올 것
	 */
	public void reqFail(ReqFail reqFail, String myId, ChannelHandlerContext ctx){
		Channel socket = ctx.channel();
		try{
			//실패 메세지 읽어옴
			List<Packet> packetList = failDAO.getFailMessage(myId, reqFail.getRoomId());

			Result result = new Result();
			result.setPacket(packetList);
			result.setResult(true);

			String resultStr = mapper.writeValueAsString(result);
			socket.writeAndFlush(resultStr);
		}catch(Exception e){
			e.printStackTrace();
			socket.writeAndFlush("{\"result\":false}");
			return;
		}

	}
	/**
	 * 채팅방 나갔을 때
	 */
	public void exitRoom(ExitRoom exitRoom, String myId, ChannelHandlerContext ctx){
		Channel socket = ctx.channel();
		try{
			String roomId = exitRoom.getRoomId();

			//유효성 검사
			if(messageDAO.findRoomList(myId, roomId)){
				messageDAO.deleteRoomList(myId, roomId);
				messageDAO.deleteRoomUser(roomId, myId);
				socket.writeAndFlush("{\"result\":true}");
			}
		}catch(Exception e){
			e.printStackTrace();
			socket.writeAndFlush("{\"result\":false}");
			return;
		}
	}

	/**
	 * 메세지 전송 처리
	 */
	public void sendMsg(SendMsg msg, String myId, ChannelHandlerContext ctx) {
		Channel socket = ctx.channel();
		try {
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
				socket.writeAndFlush("{\"result\":true}");
			}
		} catch (Exception e){
			e.printStackTrace();
			socket.writeAndFlush("{\"result\":false}");
		}
	}
	
	/**
	 * 채팅방 생성 로직 
	 */
	public void createRoom(CreateRoom req, String myId, ChannelHandlerContext ctx){
		Channel ch = ctx.channel();
		try{
			//방정보 생성
			String roomId = indexDAO.increaseRoomIndex();	//방 인덱스
			List<String> idList = req.getIdList();
			if(idList==null)
				throw new Exception("초대할 친구 리스트가 비었습니다.");

			//방접속 정보 저장
			messageDAO.saveRoomList(myId, roomId);
			messageDAO.saveRoomUser(roomId, myId);
			MessageListener listener = new MessageListenerImpl(ch, myId, roomId, failDAO);
			redisContainer.addMessageListener(listener, new ChannelTopic(roomId));	//리스너 자원 관리
			listenerMap.get(myId).add(listener);

			for(String id : idList){
				Member member = new Member();
				member.setId(id); 
				//존재하는 사용자 일 경우에만
				if(memberService.isExistsMember(member) == true && id.equals(myId) == false ){
					messageDAO.saveRoomList(id, roomId);
					messageDAO.saveRoomUser(roomId, id);
					TokenListItem item = memberService.getTokenListItem(id);
					String friendToken = item.getToken();
					Channel socketChannel;
					if( (socketChannel = tokenChannelMap.get(friendToken)) != null){
						MessageListener listenerForSubscriber = new MessageListenerImpl(socketChannel, id, roomId, failDAO);
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

			//응답

			ch.writeAndFlush("{\"result\":true,\"roomId\":\""+roomId+"\"}");
		}catch(Exception e){
			e.printStackTrace();
			ch.writeAndFlush("{\"result\":false}");
			return;
		}
	}
}
