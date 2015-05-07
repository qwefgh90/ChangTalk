package com.chang.im.dto;

import org.json.JSONObject;

import com.chang.im.chat.protocol.JsonTransformer;

public class Packet {
	String roomId;
	String fromId;
	Long timestamp;
	String content;
	String messageIndex;

	public String getMessageIndex() {
		return messageIndex;
	}
	public void setMessageIndex(String messageIndex) {
		this.messageIndex = messageIndex;
	}
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	public String getFromId() {
		return fromId;
	}
	public void setFromId(String fromID) {
		this.fromId = fromID;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
