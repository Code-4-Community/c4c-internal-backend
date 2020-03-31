package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;

import io.vertx.ext.web.RoutingContext;

public class CreateUserException extends RuntimeException implements HandledException {

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleCreateUser(ctx, this);
  }

  public enum UsedField {
    EMAIL, USERNAME, BOTH
  }

  private UsedField usedField;

  public CreateUserException(UsedField usedField) {
    super();
    this.usedField = usedField;
  }

  public UsedField getUsedField() {
    return usedField;
  }
}
