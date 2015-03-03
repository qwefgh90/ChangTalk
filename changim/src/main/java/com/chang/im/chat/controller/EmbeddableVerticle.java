package com.chang.im.chat.controller;

import org.vertx.java.core.Vertx;

/**
 * @author Keesun Baik
 */
public interface EmbeddableVerticle {

    void start(Vertx vertx);

    String host();

    int port();

}
