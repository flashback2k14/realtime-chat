package com.flbk;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Chat {

	private String id;
	private String chatId;
	private String chatDesc;
	private List<Message> messages = new ArrayList<>();
	
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
		JsonArray msgs = json.getJsonArray("messages");
		msgs.forEach(msg -> {
			this.addMessage(new Message((JsonObject)msg));
		});
	}
	
	public JsonObject toJson(){
		JsonObject j = new JsonObject()
				.put("chatId", this.chatId)
				.put("chatDesc", this.chatDesc)
				.put("messages", new JsonArray(messages));
		if(this.id != null && !this.id.isEmpty()){
			System.out.println("ID NOT EMPTY");
			j.put("_id", this.id);
		}
		
		return j;
	}
	
	public void addMessage(Message msg){
		this.messages.add(msg);
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
