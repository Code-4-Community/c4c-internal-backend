package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class InvalidTokenException extends RuntimeException implements HandledException {

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleInvalidToken(ctx);
  }
}
