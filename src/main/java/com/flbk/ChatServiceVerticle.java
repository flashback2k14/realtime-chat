package com.flbk;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class ChatServiceVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(ChatServiceVerticle.class);
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		// create router
		Router router = Router.router(vertx);
		
		// handle events
		router.route("/eventbus/*").handler(eventBusHandler());
		// handle api
		router.mountSubRouter("/api", chatApiRouter());
		// handle errors
		router.route().failureHandler(errorHandler());
		// serve frontend
		router.route().handler(staticHandler());
				
		// create server
		vertx
			.createHttpServer()
			.requestHandler(router::accept)
			.listen(Integer.getInteger("port", 7070), result -> {
				if (result.succeeded()) {
					startFuture.complete();
				} else {
					startFuture.fail(result.cause());
				}
			});
	}

	private SockJSHandler eventBusHandler() {
		BridgeOptions options = new BridgeOptions()
				.addOutboundPermitted(new PermittedOptions().setAddressRegex("chat\\.[0-9]+"));
		
		return SockJSHandler.create(vertx).bridge(options, event -> {
			if (event.type() == BridgeEventType.SOCKET_CREATED) {
				logger.info("A chat socket was created!");
			}
			if (event.type() == BridgeEventType.SOCKET_CLOSED) {
				logger.info("A chat socket was closed!");
			}
			
			event.complete(true);
		});
	}
	
	private Router chatApiRouter() {
		JsonObject dbConf = new JsonObject()
    			.put("db_name", "chats")
    			.put("connection_string", "mongodb://localhost:27017");
		DeploymentOptions opts = new DeploymentOptions().setConfig(dbConf);
		
		ChatRepository repository = new ChatRepository(vertx.sharedData(), vertx, dbConf);
		ChatHandler chatHandler = new ChatHandler(repository);
		
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		
		router.route().consumes("application/json");
		router.route().produces("application/json");
		
//		router.route("/chats/:id").handler(chatHandler::initChatInSharedData);
		router.get("/chats/:id").handler(chatHandler::handleGetChat);
//		router.patch("/chats/:id").handler(chatHandler::handleChangedChatMessage);
		router.post("/chats").handler(chatHandler::handleAddChat);
		
		return router;
	}

	private ErrorHandler errorHandler() {
		return ErrorHandler.create(true);
	}
	
	private StaticHandler staticHandler() {
		return StaticHandler.create().setCachingEnabled(false);
	}
}
