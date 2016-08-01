package com.flbk;

import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class ChatRepository {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static String COLLECTION = "chats";
	private MongoClient mongo;
	
	public ChatRepository(Vertx vertx, JsonObject dbConfig) {
		this.mongo = MongoClient.createShared(vertx, dbConfig);
	}
	
	public void getAllChats(Handler<AsyncResult<List<Chat>>> handler) {
		logger.info("Fetching all chats from the database");

		mongo.find(COLLECTION, new JsonObject(), ar -> {
			if (ar.succeeded()) {
				List<JsonObject> json = ar.result();
				List<Chat> chats = json.stream().map(j -> {
					return new Chat(j.getString("_id"), j.getString("chatId"), j.getString("chatDesc"));
				}).collect(Collectors.toList());
				handler.handle(Future.succeededFuture(chats));
			} else {
				handler.handle(Future.failedFuture(ar.cause().getMessage()));
			}
		});
	}
	
	public void getChatById(String chatId, Handler<AsyncResult<Chat>> handler) {
		logger.info("Fetching chat with chatId: " + chatId);

		mongo.find(COLLECTION, new JsonObject().put("chatId", chatId), ar -> {
			if (ar.succeeded()) {
				Chat c = null;
				List<JsonObject> json = ar.result();
				if (json.size() > 0) {
					c = new Chat(json.get(0));
				}
				handler.handle(Future.succeededFuture(c));
			} else {
				handler.handle(Future.failedFuture(ar.cause().getMessage()));
			}
		});
	}
	
	public void saveChat(Chat chat, Handler<AsyncResult<Chat>> handler) {
		logger.info("Save chat: " + chat.getChatId());

		chat.resetDbId(); // Reset id if already set

		mongo.insert(COLLECTION, chat.toJson(), ar -> {
			if (ar.succeeded()) {
				String id = ar.result();
				chat.set_id(id);
				handler.handle(Future.succeededFuture(chat));
			} else {
				handler.handle(Future.failedFuture(ar.cause().getMessage()));
			}
		});
	}
	
	public void saveMessage(String chatId, Message msg, Handler<AsyncResult<Message>> handler) {
		logger.info("Add message to chat: " + chatId);
		
		JsonObject query = new JsonObject().put("chatId", chatId);
		JsonObject update = new JsonObject().put("$push", new JsonObject().put("messages", msg.toJson()));
		
		mongo.update(COLLECTION, query, update, ar -> {
			if (ar.succeeded()) {
				handler.handle(Future.succeededFuture(msg));
			} else {
				handler.handle(Future.failedFuture(ar.cause().getMessage()));
			}
		});
	}
}
