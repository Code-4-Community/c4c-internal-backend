package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;
import io.vertx.ext.web.RoutingContext;

public class UserDoesNotExistException extends RuntimeException implements HandledException {
  private int userId;

  public UserDoesNotExistException(int userId) {
    this.userId = userId;
  }

  public int getUserId() {
    return userId;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleUserDoesNotExist(ctx, this);
  }
}
