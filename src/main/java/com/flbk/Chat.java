package com.flbk;

public class Chat {

	private String id;
	private String name;
	private String message;
	
	public Chat(String id, String name, String message) {
		this.id = id;
		this.name = name;
		this.message = message;
	}
	
	public Chat(String id, String name) {
		this.id = id;
		this.name = name;
		this.message = "";
	}
	
	public Chat(String id) {
		this.id = id;
		this.name = "";
		this.message = "";
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "Chat [id=" + id + ", name=" + name + ", message=" + message + "]";
	}
}
