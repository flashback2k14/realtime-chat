package com.flbk;

import java.util.Optional;

import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

public class ChatRepository {

	private SharedData sharedData;
	
	public ChatRepository(SharedData sharedData) {
		this.sharedData = sharedData;
	}
	
	public Optional<Chat> getById(String chatId) {
		LocalMap<String, String> chatSharedData = this.sharedData.getLocalMap(chatId);
		
		return Optional.of(chatSharedData)
				.filter(lm -> !lm.isEmpty())
				.map(this::convertToChat);
	}
	
	public void save(Chat chat) {
		LocalMap<String, String> chatSharedData = this.sharedData.getLocalMap(chat.getId());
		
		chatSharedData.put("id", chat.getId());
		chatSharedData.put("name", chat.getName());
		chatSharedData.put("message", chat.getMessage());
	}
	
	private Chat convertToChat(LocalMap<String, String> chat) {
		return new Chat(
				chat.get("id"),
				chat.get("name"),
				chat.get("message"));
	}
}
