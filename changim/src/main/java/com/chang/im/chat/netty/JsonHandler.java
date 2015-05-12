package com.chang.im.chat.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.chang.im.chat.protocol.CreateRoom;
import com.chang.im.chat.protocol.ExitRoom;
import com.chang.im.chat.protocol.ReqFail;
import com.chang.im.chat.protocol.ReqMsg;
import com.chang.im.chat.protocol.Result;
import com.chang.im.chat.protocol.SendMsg;
import com.chang.im.dao.FailDAO;
import com.chang.im.dao.IndexDAO;
import com.chang.im.dao.MessageDAO;
import com.chang.im.dto.Member;
import com.chang.im.dto.Packet;
import com.chang.im.dto.TokenListItem;
import com.chang.im.service.MemberService;
import com.chang.im.service.MessageListenerImpl;
import com.chang.im.util.IMUtil;
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
	ConcurrentHashMap<String,Map<String, MessageListener>> idChannelListenerMap;
	@Resource
	ConcurrentHashMap<String,String> tokenIdMap;
	@Resource
	ConcurrentHashMap<String,Channel> idChannelMap;
	
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
		String myId = tokenIdMap.get(msg.get("token"));
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
			processClientAck(result,myId,ctx);
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
		JSONObject result = null;
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
			result = new JSONObject();
			result.put("type", Protocol.reqAllID.name());
			result.put("result", true);
			result.put("response", arr);
			
		}catch(Exception e){
			e.printStackTrace();

			result = new JSONObject();
			try {
				result.put("type", Protocol.reqAllID.name());
				result.put("result", false);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}finally{
			socket.writeAndFlush(result.toString());
		}
	}

	/**
	 * 메세지에 대한 클라이언트의 확인 응답
	 * 실패 메세지 수신에 대한 확인 응답
	 */
	public void processClientAck(Result result, String myId, ChannelHandlerContext ctx){
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
		JSONObject result = null;
		try{
			//유효성 검사
			if(messageDAO.findRoomList(myId, reqMsg.getRoomId()) == true){

				//시간 기준으로 메세지 읽어옴
				Set<String> messageSet = messageDAO.readMessage(reqMsg.getRoomId(), reqMsg.getLastTime());
				JSONArray packetList = new JSONArray();
				for(String messageStr : messageSet){
					Packet packet = mapper.readValue(messageStr, Packet.class);
					Map map = IMUtil.objectToMap(packet);
					packetList.put(map);
				}
				
				result = new JSONObject();
				result.put("result", true);
				result.put("type", Protocol.reqMsg.name());
				result.put("response", packetList);
			}
		}catch(Exception e){
			e.printStackTrace();
			result = new JSONObject();
			try {
				result.put("result", false);
				result.put("type", Protocol.reqMsg.name());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}finally{
			socket.writeAndFlush(result.toString());
		}
	}

	/**
	 * 실패한 메세지 리스트 요청
	 * Protocol.sendMsgToCli.name() 채널로 응답 올 것
	 */
	public void reqFail(ReqFail reqFail, String myId, ChannelHandlerContext ctx){
		Channel socket = ctx.channel();
		JSONObject result = null;
		try{
			//실패 메세지 읽어옴
			List<Packet> failList = failDAO.getFailMessage(myId, reqFail.getRoomId());

			JSONArray packetList = new JSONArray();
			for(Packet packet : failList){
				Map map = IMUtil.objectToMap(packet);
				packetList.put(map);
			}
			
			result = new JSONObject();
			result.put("result", true);
			result.put("type", Protocol.reqFail.name());
			result.put("response", packetList);

		}catch(Exception e){
			e.printStackTrace();
			result = new JSONObject();
			try {
				result.put("result", false);
				result.put("type", Protocol.reqFail.name());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}finally{
			socket.writeAndFlush(result.toString());
		}
	}
	
	/**
	 * 채팅방 나갔을 때
	 */
	public void exitRoom(ExitRoom exitRoom, String myId, ChannelHandlerContext ctx){
		Channel socket = ctx.channel();
		JSONObject result = null;
		try{
			String roomId = exitRoom.getRoomId();

			//유효성 검사
			if(messageDAO.findRoomList(myId, roomId)){
				messageDAO.deleteRoomList(myId, roomId);
				messageDAO.deleteRoomUser(roomId, myId);
				
				result = new JSONObject();
				result.put("result", true);
				result.put("type", Protocol.exitRoom.name());
			}
		}catch(Exception e){
			e.printStackTrace();
			result = new JSONObject();
			try {
				result.put("result", false);
				result.put("type", Protocol.exitRoom.name());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}finally{
			socket.writeAndFlush(result.toString());
		}
	}

	/**
	 * 메세지 전송 처리
	 */
	public void sendMsg(SendMsg msg, String myId, ChannelHandlerContext ctx) {
		Channel socket = ctx.channel();
		JSONObject result = null;
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
				result = new JSONObject();
				result.put("result", true);
				result.put("type", Protocol.sendMsg.name());
			}
		} catch (Exception e){
			e.printStackTrace();
			result = new JSONObject();
			try {
				result.put("result", false);
				result.put("type", Protocol.sendMsg.name());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}finally{
			socket.writeAndFlush(result.toString());
		}
	}

	/**
	 * 채팅방 생성 로직 
	 */
	public void createRoom(CreateRoom req, String myId, ChannelHandlerContext ctx){
		Channel ch = ctx.channel();
		JSONObject result = null;
		try{
			//방정보 생성
			String roomId = indexDAO.increaseRoomIndex();	//방 인덱스
			List<String> idList = req.getIdList();
			if(idList==null)
				throw new Exception("초대할 친구 리스트가 비었습니다.");

			//방접속 정보 저장
			messageDAO.saveRoomList(myId, roomId);
			messageDAO.saveRoomUser(roomId, myId);
			MessageListener listener = new MessageListenerImpl(myId, roomId, failDAO);
			redisContainer.addMessageListener(listener, new ChannelTopic(roomId));	//리스너 자원 관리
			idChannelListenerMap.get(myId).put(roomId, listener);

			for(String id : idList){
				Member member = new Member();
				member.setId(id); 
				//존재하는 사용자 일 경우에만
				if(memberService.isExistsMember(member) == true && id.equals(myId) == false ){
					messageDAO.saveRoomList(id, roomId);
					messageDAO.saveRoomUser(roomId, id);
					TokenListItem item = memberService.getTokenListItem(id);
					String friendToken = item.getToken();
					MessageListener listenerForSubscriber = new MessageListenerImpl(id, roomId, failDAO);
					redisContainer.addMessageListener(listenerForSubscriber, new ChannelTopic(roomId));	
					idChannelListenerMap.get(id).put(roomId, listenerForSubscriber);
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
			result = new JSONObject();
			result.put("result", true);
			result.put("type", Protocol.createRoom.name());
			JSONObject body = new JSONObject();
			body.put("roomId", roomId);
			result.put("response", body);

		}catch(Exception e){
			e.printStackTrace();

			//응답
			result = new JSONObject();
			try {
				result.put("result", false);
				result.put("type", Protocol.createRoom.name());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}finally{
			ch.writeAndFlush(result.toString());
		}
	}
}
