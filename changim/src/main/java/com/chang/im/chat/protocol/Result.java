package com.chang.im.chat.protocol;

public class Result {
	boolean result;
	String roomId;
	String messageIndex;
	SendMsgToCli msg;
	
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
	public SendMsgToCli getMsg() {
		return msg;
	}
	public void setMsg(SendMsgToCli msg) {
		this.msg = msg;
	}
	
}
