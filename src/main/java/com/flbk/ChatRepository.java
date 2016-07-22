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
		logger.info("Mongo client was created");
	}
	
	public void getAllChats(Handler<AsyncResult<List<Chat>>> handler){
		logger.info("Fetching all chats from the database");
		mongo.find(COLLECTION, new JsonObject(), ar -> {
			if(ar.succeeded()){
				List<JsonObject> json = ar.result();
				List<Chat> chats = json.stream().map(j -> {
					return new Chat(j.getString("_id"), j.getString("chatId"), j.getString("chatDesc"));
				}).collect(Collectors.toList());
				logger.info("Fetching chats succeeded");
				handler.handle(Future.succeededFuture(chats));
			}else{
				logger.info("Fetching chats failed");
				handler.handle(Future.failedFuture(ar.cause()));
			}
		});
	}
	
	public void getChatById(String chatId, Handler<AsyncResult<Chat>> handler){
		logger.info("Fetching chat with chatId: " + chatId);
		mongo.find(COLLECTION, new JsonObject().put("chatId", chatId), ar -> {
			if(ar.succeeded()){
				Chat c = null;
				List<JsonObject> json = ar.result();
				if(json.size() > 0){
					c = new Chat(json.get(0));
				}
				logger.info("Fetching chat with chatId: " + chatId + " succeeded");
				handler.handle(Future.succeededFuture(c));
			}else{
				logger.info("Fetching chat with chatId: " + chatId + " failed");
				handler.handle(Future.failedFuture(ar.cause()));
			}
		});
	}
	
	public void saveChat(Chat chat, Handler<AsyncResult<Chat>> handler){
		logger.info("Save chat: " + chat.toJson().encodePrettily());
		chat.resetDbId(); // Reset id if already set
		mongo.insert(COLLECTION, chat.toJson(), ar -> {
			if(ar.succeeded()){
				String id = ar.result();
				chat.set_id(id);
				handler.handle(Future.succeededFuture(chat));
				logger.info("Saving chat succeeded");
			}else{
				logger.info("Saving chat failed");
				handler.handle(Future.failedFuture(ar.cause().getMessage()));
			}
		});
	}
	
	public void saveMessage(String chatId, Message msg, Handler<AsyncResult<Message>> handler){
		logger.info("Add message " + msg.toJson().encodePrettily() + " to chat " + chatId);
		
		JsonObject query = new JsonObject().put("chatId", chatId);
		JsonObject update = new JsonObject().put(
				"$push",
				new JsonObject()
				.put("messages", msg.toJson()));
		
		mongo.update(COLLECTION, query, update, ar -> {
			if(ar.succeeded()){
				handler.handle(Future.succeededFuture(msg));
				logger.info("Adding message succeeded");
			}else{
				logger.info("Adding message failed");
				handler.handle(Future.failedFuture(ar.cause().getMessage()));
			}
		});
	}


}
