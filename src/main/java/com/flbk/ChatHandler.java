package com.flbk;

import java.util.List;
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

	// public void initChatInSharedData(RoutingContext context) {
	// String chatId = context.request().getParam("id");
	//
	// Optional<Chat> chat = this.repository.getById(chatId);
	//
	// if (!chat.isPresent()) {
	// this.repository.save(new Chat(chatId));
	// }
	//
	// context.next();
	// }

	public void handleAddChat(RoutingContext context) {
		Chat c = Json.decodeValue(context.getBodyAsString(), Chat.class);

		this.repository.getAllChats(ar -> {
			if (ar.succeeded()) {
				List<Chat> chats = ar.result();
				if (chats.contains(c)) {
					context.response().putHeader("content-type", "application/json").setStatusCode(400)
						.end(
							new JsonObject().put("error", "A chat with the provided idendtifier already exists")
							.encodePrettily());
				} else {
					this.repository.saveChat(c, ar1 -> {
						if (ar1.succeeded()) {
							context.response().putHeader("content-type", "application/json").setStatusCode(201)
									.end(ar1.result().toJson().encodePrettily());
						} else {
							context.response().putHeader("content-type", "application/json").setStatusCode(500).end();
						}
					});
				}
			} else {
				context.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}

	public void handleGetChat(RoutingContext context) {
		String chatId = context.request().getParam("id");
		System.out.println("GET CHAT WITH ID: " + chatId);
		this.repository.getById(chatId, ar -> {
			if (ar.succeeded()) {
				if (Objects.nonNull(ar.result())) {
					System.out.println("RESP: " + ar.result().toJson().encodePrettily());
					context.response().putHeader("content-type", "application/json").setStatusCode(200)
							.end(ar.result().toJson().encodePrettily());
				} else {
					context.response().putHeader("content-type", "application/json").setStatusCode(404).end();
				}
			} else {
				context.response().putHeader("content-type", "application/json").setStatusCode(500)
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

		Message msg = new Message(context.getBodyAsJson().getString("author"),
				context.getBodyAsJson().getString("content"));

		this.repository.saveMessage(chatId, msg, ar -> {
			if (ar.succeeded()) {
				context.vertx().eventBus().publish("chat." + chatId, context.getBodyAsString());
				context.response().setStatusCode(200).end();
			} else {
				context.vertx().eventBus().publish("chat." + chatId, "Sending messages failed");
				context.response().setStatusCode(500).end(ar.cause().getMessage());
			}
		});

		// String chatId = context.request().getParam("id");
		// Chat chatRequest = new Chat(chatId,
		// context.getBodyAsJson().getString("name"),
		// context.getBodyAsJson().getString("message"));
		//
		// this.repository.save(chatRequest);
		//
		// context.vertx().eventBus().publish("chat." + chatId,
		// context.getBodyAsString());
		//
		// context.response().setStatusCode(200).end();
	}

	public void handleGetAllChats(RoutingContext context) {
		this.repository.getAllChats(ar -> {
			if (ar.succeeded()) {
				context.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(Json.encodePrettily(ar.result()));
			} else {
				context.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
}
