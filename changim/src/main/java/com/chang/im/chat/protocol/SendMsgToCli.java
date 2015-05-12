package com.chang.im.chat.protocol;

import org.json.JSONObject;

import com.chang.im.chat.netty.JsonHandler;

/**
 * 클라이언트에게 보내는 채팅방 메세지
 * @author cheochangwon
 *
 */
public class SendMsgToCli{
	String roomId;
	String messageIndex;
	String from;
	String content;
	Long timestamp;
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	public String getMessageIndex() {
		return messageIndex;
	}
	public void setMessageIndex(String messageIndex) {
		this.messageIndex = messageIndex;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getContent() {
		return content;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
