package com.chang.im.chat.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.chang.im.dto.LoginInfo;
import com.chang.im.service.MemberService;

@Sharable
//공유되는 핸들러라는 사실을 네티에게 알려주는 어노테이션
public class JsonAndAuthDecoder extends MessageToMessageDecoder<String>{

	@Autowired
	MemberService memberService;

	@Resource
	ConcurrentHashMap<String,Map<String, MessageListener>> idChannelListenerMap;
	@Resource
	ConcurrentHashMap<String,String> tokenIdMap;
	@Resource
	ConcurrentHashMap<String,Channel> idChannelMap;

	@Override
	protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out)
			throws Exception {
		JSONObject object = new JSONObject(msg);
		String token = (String)object.get("token");
		if(token==null){
			throw new Exception("올바르지 않은 토큰입니다.");
		}else{
			/**
			 * 토큰 인증 처리
			 */
			LoginInfo loginInfo = memberService.getUserInfo(token);	//새로운 채널
			if(loginInfo == null)
				throw new Exception("존재하지 않는 토큰입니다.");
			String id = loginInfo.getId();
			
			tokenIdMap.put(token, id);
			Channel oldChannel = idChannelMap.get(id);	//이미 등록된 채널
			Channel newChannel = ctx.channel();			//새로운 채널
			if(oldChannel == null || oldChannel != newChannel){	//사용자의 자료구조에 변경 사항 있는지
				/**
				 * 필요한 자료구조 관리
				 * 1)토큰 - 아이디
				 * 2)아이디 - 채널
				 * 3)아이디 - 리스너 컨테이너(해시맵)
				 */
				if(oldChannel==null){
					idChannelMap.put(id, newChannel);
				}else{
					oldChannel.close();	
					idChannelMap.put(id, newChannel);	//아이디 - 채널
				}
				if(false == idChannelListenerMap.containsKey(id))	//아디 - 리스너 컨테이더 없을때
					idChannelListenerMap.put(id, new ConcurrentHashMap<String, MessageListener>());//아이디 - 리스너 컨테이너 등록
			}

			//인증 성공
			out.add(new JSONObject(msg));	//JSONObject 로 변환
		}
	}
}