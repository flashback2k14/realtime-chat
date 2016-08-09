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
		//String address = "chat-ids";
		// String chatId = context.request().getParam("id");
		// String address = "chat." + chatId;
		String address = "xyz";

		this.repository.getAllChats(ar -> {
			if (ar.succeeded()) {
				List<Chat> chats = ar.result();
				if (chats.contains(c)) {
					context.response()
						.putHeader("content-type", "application/json")
						.setStatusCode(400)
						.end(createErrorObject("A chat with the provided idendtifier already exists"));
				} else {
					this.repository.saveChat(c, ar1 -> {
						if (ar1.succeeded()) {
							context.vertx().eventBus()
								.publish(address, createResponseObject(0, ar1.result().toJson()));
							context.response().setStatusCode(201).end();
						} else {
							context.vertx().eventBus()
								.publish(address, createErrorObject("Addition failed: Respond with 500 INTERNAL SERVER ERROR"));
							context.response()
								.putHeader("content-type", "application/json")
								.setStatusCode(500)
								.end(createErrorObject("Addition failed: Respond with 500 INTERNAL SERVER ERROR"));
						}
					});
				}
			} else {
				context.vertx().eventBus().publish(address, "Chat Id already exists!");
				context.response().setStatusCode(500).end();
			}
		});
	}

	public void handleGetChat(RoutingContext context) {
		
		logger.info("Handle request to get a single chat");
		String chatId = context.request().getParam("id");

		this.repository.getChatById(chatId, ar -> {
			if (ar.succeeded()) {
				if (Objects.nonNull(ar.result())) {
					context.response().putHeader("content-type", "application/json")
							.setStatusCode(200)
							.end(ar.result().toJson().encodePrettily());
				} else {
					context.response().putHeader("content-type", "application/json")
							.setStatusCode(404)
							.end(ar.cause().getMessage());
				}
			} else {
				context.response().putHeader("content-type", "application/json")
						.setStatusCode(500)
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
				context.vertx().eventBus().publish(address, createResponseObject(1, msg.toJson()));
				context.response().setStatusCode(201).end();
			} else {
				context.vertx().eventBus().publish(address, "Sending messages failed");
				context.response()
					.putHeader("content-type", "application/json")
					.setStatusCode(500)
					.end(createErrorObject(ar.cause().getMessage()));
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

	private String createErrorObject(String msg) {
		return new JsonObject().put("error", msg).encodePrettily();
	}

	private String createResponseObject(int type, JsonObject obj) {
		switch (type) {
			case 0:
				return new JsonObject()
									.put("type", "chat")
									.put("response", obj)
									.encodePrettily();
			case 1:
				return new JsonObject()
									.put("type", "message")
									.put("response", obj)
									.encodePrettily();
			default:
				return "Invalid Type!";
		}
	}
}
