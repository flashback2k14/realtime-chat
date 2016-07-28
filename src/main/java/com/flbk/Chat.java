package com.flbk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Chat {

	private String id;
	private String chatId;
	private String chatDesc;
	private List<Message> messages = new ArrayList<>();
	
	public Chat(){}
	
	public Chat(String id, String chatId, String chatDesc) {
		this.id = id;
		this.chatId = chatId;
		this.chatDesc = chatDesc;
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
	
	public void setChatId(String chatId){
		this.chatId = chatId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public String getChatDesc() {
		return chatDesc;
	}

	public void setChatDesc(String chatDesc) {
		this.chatDesc = chatDesc;
	}

	
	
	@Override
	public String toString() {
		return "Chat [id=" + id
		       + ", chatId="
		       + chatId
		       + ", chatDesc="
		       + chatDesc
		       + ", messages="
		       + messages
		       + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chatId == null) ? 0 : chatId.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof Chat)){
			return false;
		}
		Chat that = (Chat)o;
		return this.chatId.equals(that.getChatId());
		
	}
}
