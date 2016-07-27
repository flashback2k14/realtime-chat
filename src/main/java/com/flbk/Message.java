package com.flbk;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

public class Message {

	private String mid;
	private String author;
	private String content;
	
	public Message() {
		mid = UUID.randomUUID().toString();
	}
	
	public Message(String author, String content) {
		mid = UUID.randomUUID().toString();
		this.author = author;
		this.content = content;
	}
	
	public Message(JsonObject json) {
		this.mid = json.getString("mid");
		this.author = json.getString("author");
		this.content = json.getString("content");
	}
	
	public JsonObject toJson() {
		return new JsonObject()
				.put("mid", mid)
				.put("author", author)
				.put("content", content);
	}
	
	public String getMid() {
		return mid;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
