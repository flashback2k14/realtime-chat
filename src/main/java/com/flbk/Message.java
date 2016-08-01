package com.flbk;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import io.vertx.core.json.JsonObject;

public class Message {

	private String mid;
	private String author;
	private String content;
	private String created = "";
	
	public Message() {
		mid = UUID.randomUUID().toString();
		addTimestamp();
	}
	
	public Message(String author, String content) {
		mid = UUID.randomUUID().toString();
		this.author = author;
		this.content = content;
		addTimestamp();
	}
	
	public Message(JsonObject json) {
		this.mid = json.getString("mid");
		this.author = json.getString("author");
		this.content = json.getString("content");
		String ts = json.getString("created");
		if (Objects.isNull(ts)) {
			this.created = "";
		} else {
			this.created = json.getString("created");
		}
	}
	
	public JsonObject toJson() {
		return new JsonObject()
				.put("mid", mid)
				.put("author", author)
				.put("content", content)
				.put("created", created);
	}
	
	public String getCreated() {
		return created;
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
	
	private void addTimestamp() {
		if (this.created == null || this.created.isEmpty()) {
			this.created = String.valueOf(new Date().getTime());
		}
	}

	@Override
	public String toString() {
		return "Message [mid=" + mid
		       + ", author="
		       + author
		       + ", content="
		       + content
		       + ", createdAt="
		       + created
		       + "]";
	}
}
