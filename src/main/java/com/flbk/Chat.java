package com.flbk;

import io.vertx.core.json.JsonObject;

public class Chat {

	private String id;
	private String chatId;
	private String chatDesc;
	
	public Chat(){}
	
	public Chat(String id, String name, String message) {
		this.chatId = id;
		this.chatDesc = name;
	}
	
	public Chat(String id, String name) {
		this.chatId = id;
		this.chatDesc = name;
	}
	
	public Chat(String id) {
		this.chatId = id;
		this.chatDesc = "";
	}
	
	public Chat(JsonObject json){
		this.id = json.getString("_id");
		this.chatId = json.getString("chatId");
		this.chatDesc = json.getString("chatDesc");
	}
	
	public JsonObject toJson(){
		JsonObject j = new JsonObject()
				.put("chatId", this.chatId)
				.put("chatDesc", this.chatDesc);
		if(this.id != null && !this.id.isEmpty()){
			System.out.println("ID NOT EMPTY");
			j.put("_id", this.id);
		}
		return j;
	}
	
	public void resetDbId(){
		this.id = null;
	}

	public String get_id() {
		return id;
	}

	public void set_id(String _id) {
		this.id = _id;
	}

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public String getChatDesc() {
		return chatDesc;
	}

	public void setChatDesc(String chatDesc) {
		this.chatDesc = chatDesc;
	}

	@Override
	public String toString() {
		return "Chat [id=" + chatId + ", description=" + chatDesc + "]";
	}
}
