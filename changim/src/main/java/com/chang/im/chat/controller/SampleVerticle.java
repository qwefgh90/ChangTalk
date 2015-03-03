package com.chang.im.chat.controller;

import org.springframework.stereotype.Component;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonObject;

import com.nhncorp.mods.socket.io.SocketIOServer;
import com.nhncorp.mods.socket.io.SocketIOSocket;
import com.nhncorp.mods.socket.io.impl.Configurer;
import com.nhncorp.mods.socket.io.impl.DefaultSocketIOServer;

/**
 * @author Keesun Baik
 */
@Component
public class SampleVerticle extends DefaultEmbeddableVerticle {

	private SocketIOServer io;

	@Override
	public void start(Vertx vertx) {
		HttpServer server = vertx.createHttpServer();
		io = new DefaultSocketIOServer(vertx, server);
		io.sockets().onConnection(new Handler<SocketIOSocket>() {
			public void handle(final SocketIOSocket socket) {
				socket.emit("welcome : "+"Chang im chat is started");

				socket.on("echo", new Handler<JsonObject>() {
					public void handle(JsonObject msg) {
						socket.emit("echo","changim : "+ msg);
					}
				});
			}
		});
		server.listen(9090);
	}

	public SocketIOServer getIo() {
		return io;
	}
}