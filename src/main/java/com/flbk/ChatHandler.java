package com.flbk;

import java.util.Optional;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class ChatHandler {

	private ChatRepository repository;
	
	public ChatHandler(ChatRepository repository) {
		this.repository = repository;
	}
	
	public void initChatInSharedData(RoutingContext context) {
		String chatId = context.request().getParam("id");
		
		Optional<Chat> chat = this.repository.getById(chatId);
		
		if (!chat.isPresent()) {
			this.repository.save(new Chat(chatId));
		}
		
		context.next();
	}
	
	public void handleGetChat(RoutingContext context) {
		String chatId = context.request().getParam("id");
		Optional<Chat> chat = this.repository.getById(chatId);
		
		if (chat.isPresent()) {
			context.response()
				.putHeader("content-type", "application/json")
				.setStatusCode(200)
				.end(Json.encodePrettily(chat.get()));
		} else {
			context.response()
				.putHeader("content-type", "application/json")
				.setStatusCode(404)
				.end();
		}
	}
	
	public void handleChangedChatMessage(RoutingContext context) {
		String chatId = context.request().getParam("id");
		Chat chatRequest = new Chat(
				chatId, 
				context.getBodyAsJson().getString("name"),
				context.getBodyAsJson().getString("message")
		);
		
		this.repository.save(chatRequest);
		
		context.vertx().eventBus().publish("chat." + chatId, context.getBodyAsString());
		
		context.response()
			.setStatusCode(200)
			.end();
	}
}
