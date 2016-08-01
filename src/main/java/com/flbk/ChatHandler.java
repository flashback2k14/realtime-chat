package com.flbk;

import java.util.List;
import java.util.Objects;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class ChatHandler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ChatRepository repository;

	public ChatHandler(ChatRepository repository) {
		this.repository = repository;
	}

	public void handleAddChat(RoutingContext context) {
		
		logger.info("Handle request to add a single chat");
		Chat c = Json.decodeValue(context.getBodyAsString(), Chat.class);

		this.repository.getAllChats(ar -> {
			if (ar.succeeded()) {
				List<Chat> chats = ar.result();
				if (chats.contains(c)) {
					context.response().putHeader("content-type", "application/json").setStatusCode(400)
						.end(new JsonObject()
										.put("error", "A chat with the provided idendtifier already exists")
										.encodePrettily());
				} else {
					this.repository.saveChat(c, ar1 -> {
						if (ar1.succeeded()) {
							context.response().putHeader("content-type", "application/json").setStatusCode(201)
									.end(ar1.result().toJson().encodePrettily());
						} else {
							context.response().putHeader("content-type", "application/json").setStatusCode(500)
								.end(ar1.cause().getMessage());
						}
					});
				}
			} else {
				context.response().putHeader("content-type", "application/json").setStatusCode(500)
					.end(ar.cause().getMessage());
			}
		});
	}

	public void handleGetChat(RoutingContext context) {
		
		logger.info("Handle request to get a single chat");
		String chatId = context.request().getParam("id");

		this.repository.getChatById(chatId, ar -> {
			if (ar.succeeded()) {
				if (Objects.nonNull(ar.result())) {
					context.response().putHeader("content-type", "application/json").setStatusCode(200)
							.end(ar.result().toJson().encodePrettily());
				} else {
					context.response().putHeader("content-type", "application/json").setStatusCode(404)
							.end(ar.cause().getMessage());
				}
			} else {
				context.response().putHeader("content-type", "application/json").setStatusCode(500)
						.end(ar.cause().getMessage());
			}
		});
	}

	public void handleChangedChatMessage(RoutingContext context) {
		
		logger.info("Handle changed chat message");
		String chatId = context.request().getParam("id");
		String address = "chat." + chatId;

		Message msg = new Message(context.getBodyAsJson().getString("author"),
															context.getBodyAsJson().getString("content"));

		this.repository.saveMessage(chatId, msg, ar -> {
			if (ar.succeeded()) {
				context.vertx().eventBus().publish(address, msg.toJson().encodePrettily());
				context.response().setStatusCode(201).end();
			} else {
				context.vertx().eventBus().publish(address, "Sending messages failed");
				context.response().setStatusCode(500).end(ar.cause().getMessage());
			}
		});
	}

	public void handleGetAllChats(RoutingContext context) {
		
		logger.info("Handle fetching all chats (without messages)");
		
		this.repository.getAllChats(ar -> {
			if (ar.succeeded()) {
				context.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(Json.encodePrettily(ar.result()));
			} else {
				context.response().putHeader("content-type", "application/json").setStatusCode(500)
						.end(ar.cause().getMessage());
			}
		});
	}
}
