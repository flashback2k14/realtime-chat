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

		logger.info("Verify that chat not already exits");
		logger.info("Ask the database for all existing chats");
		this.repository.getAllChats(ar -> {
			if (ar.succeeded()) {
				List<Chat> chats = ar.result();
				if (chats.contains(c)) {
					logger.info("Chat already exits: Respond with 400 BAD REQUEST");
					context.response().putHeader("content-type", "application/json").setStatusCode(400)
						.end(
							new JsonObject().put("error", "A chat with the provided idendtifier already exists")
							.encodePrettily());
					logger.info("Chat does't exist, yet: Continue");
				} else {
					logger.info("Ask the database to add the chat");
					this.repository.saveChat(c, ar1 -> {
						if (ar1.succeeded()) {
							logger.info("Addition succeeded: Respond with 201 CREATED");
							context.response().putHeader("content-type", "application/json").setStatusCode(201)
									.end(ar1.result().toJson().encodePrettily());
						} else {
							logger.info("Addition failed: Respond with 500 INTERNAL SERVER ERROR");
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
		
		logger.info("Handle request to get a single chat");
		
		String chatId = context.request().getParam("id");
		logger.info("Ask database to fetch the chat for the provided id");
		this.repository.getChatById(chatId, ar -> {
			if (ar.succeeded()) {
				if (Objects.nonNull(ar.result())) {
					logger.info("Fetching the chat succeeded: Respond with 200 OK");
					context.response().putHeader("content-type", "application/json").setStatusCode(200)
							.end(ar.result().toJson().encodePrettily());
				} else {
					logger.info("Fetching the chat succeeded but the chat does't exist in the database:"
							+ " Respond with 404 NOT FOUND");
					context.response().putHeader("content-type", "application/json").setStatusCode(404).end();
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

		logger.info("Build message of the parameters");
		Message msg = new Message(context.getBodyAsJson().getString("author"),
				context.getBodyAsJson().getString("content"));

		logger.info("Ask database to add the message tot the chat with the provided id");
		this.repository.saveMessage(chatId, msg, ar -> {
			if (ar.succeeded()) {
				logger.info("Adding the message succeeded: Respond with 200 OK");
				String address = "chat." + chatId;
				logger.info("Publish the message to every client registerd to the address: " + address);
				context.vertx().eventBus().publish(address, msg.toJson().encodePrettily());
				context.response().setStatusCode(201).end();
			} else {
				String address = "chat." + chatId;
				logger.info("Adding message failed: Publish a error message to all clients registerd to the address: " + address);
				context.vertx().eventBus().publish(address, "Sending messages failed");
				context.response().setStatusCode(500).end(ar.cause().getMessage());
			}
		});
	}

	public void handleGetAllChats(RoutingContext context) {
		
		logger.info("Handle fetching all chats (without messages)");
		
		logger.info("Ask database to fetch all chats");
		this.repository.getAllChats(ar -> {
			if (ar.succeeded()) {
				logger.info("Fetching all chats succeeded: Respond with 200 OK");
				context.response().putHeader("content-type", "application/json").setStatusCode(200)
						.end(Json.encodePrettily(ar.result()));
			} else {
				context.response().putHeader("content-type", "application/json").setStatusCode(500).end();
			}
		});
	}
}
