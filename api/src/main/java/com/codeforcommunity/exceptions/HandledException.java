package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;

import io.vertx.ext.web.RoutingContext;

public interface HandledException {

  void callHandler(FailureHandler handler, RoutingContext ctx);
}
