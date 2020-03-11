package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;

import io.vertx.ext.web.RoutingContext;

public class AuthException extends RuntimeException implements HandledException {

    public AuthException(String message) {
        super(message);
    }

    @Override
    public void callHandler(FailureHandler handler, RoutingContext ctx) {
        handler.handleAuth(ctx);
    }
}
