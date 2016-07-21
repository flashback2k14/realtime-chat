package com.flbk;

import java.util.Objects;
import java.util.Optional;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ChatHandler {

	private ChatRepository repository;

	public ChatHandler(ChatRepository repository) {
		this.repository = repository;
	}

//	public void initChatInSharedData(RoutingContext context) {
//		String chatId = context.request().getParam("id");
//
//		Optional<Chat> chat = this.repository.getById(chatId);
//
//		if (!chat.isPresent()) {
//			this.repository.save(new Chat(chatId));
//		}
//
//		context.next();
//	}

	public void handleAddChat(RoutingContext context){
		Chat c = Json.decodeValue(context.getBodyAsString(), Chat.class);
		
		this.repository.saveChat(c, ar -> {
			if(ar.succeeded()){
				context
				.response()
				.putHeader("content-type", "application/json")
				.setStatusCode(204)
				.end(ar.result().toJson().encodePrettily());
			}else{
				context
				.response()
				.putHeader("content-type", "application/json")
				.setStatusCode(500)
				.end();
			}
		});
	}
	
	public void handleGetChat(RoutingContext context) {
		String chatId = context.request().getParam("id");
		System.out.println("GET CHAT WITH ID: " + chatId);
		this.repository.getById(chatId, ar -> {
			if (ar.succeeded()) {
				if(Objects.nonNull(ar.result())){
					System.out.println("RESP: " + ar.result().toJson().encodePrettily());
					context
					.response()
					.putHeader("content-type", "application/json")
					.setStatusCode(200)
					.end(ar.result().toJson().encodePrettily());
				}else{
					context
					.response()
					.putHeader("content-type", "application/json")
					.setStatusCode(404)
					.end();
				}
			} else {
				context
				.response()
				.putHeader("content-type", "application/json")
				.setStatusCode(500)
				.end(ar.cause().getMessage());
			}
		});

		// Optional<Chat> chat = this.repository.getById(chatId);
		//
		// if (chat.isPresent()) {
		// context.response()
		// .putHeader("content-type", "application/json")
		// .setStatusCode(200)
		// .end(Json.encodePrettily(chat.get()));
		// } else {
		// context.response()
		// .putHeader("content-type", "application/json")
		// .setStatusCode(404)
		// .end();
		// }
	}

	public void handleChangedChatMessage(RoutingContext context) {
		String chatId = context.request().getParam("id");
		Chat chatRequest = new Chat(chatId, context.getBodyAsJson().getString("name"),
				context.getBodyAsJson().getString("message"));

		this.repository.save(chatRequest);

		context.vertx().eventBus().publish("chat." + chatId, context.getBodyAsString());

		context.response().setStatusCode(200).end();
	}
}
