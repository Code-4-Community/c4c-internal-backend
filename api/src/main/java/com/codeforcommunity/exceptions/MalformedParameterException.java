package com.codeforcommunity.exceptions;

import com.codeforcommunity.rest.subrouter.FailureHandler;

import io.vertx.ext.web.RoutingContext;

public class MalformedParameterException extends RuntimeException implements HandledException {

  private final String parameterName;


  public MalformedParameterException(String parameterName) {
    this.parameterName = parameterName;
  }

  public String getParameterName() {
    return this.parameterName;
  }

  @Override
  public void callHandler(FailureHandler handler, RoutingContext ctx) {
    handler.handleMalformedParameter(ctx, this);
  }
}
