package com.flbk;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.mongo.MongoClient;

public class ChatRepository {

	private static String COLLECTION = "chats";
	
	private MongoClient mongo;
	
	private SharedData sharedData;
	
	public ChatRepository(SharedData sharedData, Vertx vertx, JsonObject dbConfig) {
		this.mongo = MongoClient.createShared(vertx, dbConfig);
		this.sharedData = sharedData;
	}
	
	public void getAllChats(Handler<AsyncResult<List<Chat>>> handler){
		System.out.println("GET ALL CHATS");
		mongo.find(COLLECTION, new JsonObject(), ar -> {
			if(ar.succeeded()){
				List<JsonObject> json = ar.result();
				List<Chat> chats = json.stream().map(j -> {
					return new Chat(j.getString("_id"), j.getString("chatId"), j.getString("chatDesc"));
				}).collect(Collectors.toList());
				handler.handle(Future.succeededFuture(chats));
			}else{
				handler.handle(Future.failedFuture(ar.cause()));
			}
		});
	}
	
	public void getById(String chatId, Handler<AsyncResult<Chat>> handler){
		System.out.println("CHAT ID: " + chatId);
		mongo.find(COLLECTION, new JsonObject().put("chatId", chatId), ar -> {
			if(ar.succeeded()){
				Chat c = null;
				List<JsonObject> json = ar.result();
				System.out.println("LIST: " + json);
				if(json.size() > 0){
					c = new Chat(json.get(0));
				}
				handler.handle(Future.succeededFuture(c));
			}else{
				handler.handle(Future.failedFuture(ar.cause()));
			}
		});
	}
	
//	public Optional<Chat> getById(String chatId) {
//		LocalMap<String, String> chatSharedData = this.sharedData.getLocalMap(chatId);
//		
//		return Optional.of(chatSharedData)
//				.filter(lm -> !lm.isEmpty())
//				.map(this::convertToChat);
//	}
	
	public void saveChat(Chat chat, Handler<AsyncResult<Chat>> handler){
		System.out.println("SAVE CHAT");
		chat.resetDbId(); // Reset id if already set
		System.out.println("ADD: " + chat.toJson().encodePrettily());
		mongo.insert(COLLECTION, chat.toJson(), ar -> {
			if(ar.succeeded()){
				String id = ar.result();
				chat.set_id(id);
				handler.handle(Future.succeededFuture(chat));
			}else{
				handler.handle(Future.failedFuture(ar.cause().getMessage()));
			}
		});
	}
	
	public void saveMessage(String chatId, Message msg, Handler<AsyncResult<Message>> handler){
		System.out.println("SAVE MESSAGE");
		
		JsonObject query = new JsonObject().put("chatId", chatId);
		JsonObject update = new JsonObject().put(
				"$push",
				new JsonObject()
				.put("messages", msg.toJson()));
		
		mongo.update(COLLECTION, query, update, ar -> {
			if(ar.succeeded()){
				handler.handle(Future.succeededFuture(msg));
			}else{
				handler.handle(Future.failedFuture(ar.cause().getMessage()));
			}
		});
	}
	
	
	public void save(Chat chat) {
		LocalMap<String, String> chatSharedData = this.sharedData.getLocalMap(chat.getChatId());
		
		chatSharedData.put("id", chat.getChatId());
		chatSharedData.put("name", chat.getChatDesc());
//		chatSharedData.put("message", chat.getMessage());
	}
	
	private Chat convertToChat(LocalMap<String, String> chat) {
		return new Chat(
				chat.get("id"),
				chat.get("name"),
				chat.get("message"));
	}
}
