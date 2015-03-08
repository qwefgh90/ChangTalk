package com.chang.im.chat.protocol;

import java.util.List;

import com.chang.im.dto.Packet;

/**
 * 응답 패킷은 단순하므로 하나의 클래스로 통일
 * 모든 응답을 처리할 수 있도록 구현됨
 * @author cheochangwon
 *
 */
public class Result {
	boolean result;
	String roomId;
	String messageIndex;
	List<Packet> packetList;
	
	public List<Packet> getPacket() {
		return packetList;
	}
	public void setPacket(List<Packet> packet) {
		this.packetList = packet;
	}
	public String getMessageIndex() {
		return messageIndex;
	}
	public void setMessageIndex(String messageIndex) {
		this.messageIndex = messageIndex;
	}
	public boolean isResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
}
