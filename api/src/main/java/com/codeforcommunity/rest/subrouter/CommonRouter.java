package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.util.JWTUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.jsonwebtoken.Claims;

import io.vertx.ext.web.handler.BodyHandler;

public class CommonRouter {
  private final FailureHandler failureHandler = new FailureHandler();
  private final JWTUtils auth;
  private final IProcessor processor;

  public CommonRouter(IProcessor processor, JWTUtils auth) {
    this.auth = auth;
    this.processor = processor;
  }

  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create(false)); // Add body handling

    router.route().failureHandler(failureHandler::handleFailure); // Add failure handling

    router.route("/protected/*").handler(this::checkAuthentication);
    router.route("/admin/*").handler(this::checkAdmin);

    return router;
  }

  private void checkAdmin(RoutingContext ctx) {
    HttpServerRequest request = ctx.request();
    HttpServerResponse response = ctx.response();
    if (!isAdmin(request)) {
      response.putHeader("location", "/").setStatusCode(401).end();
    }
    ctx.next();
  }

  public boolean isAdmin(HttpServerRequest request) {
    try {
      return (boolean) auth.getClaims(request).get("isAdmin");
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private void checkAuthentication(RoutingContext ctx) {
    HttpServerRequest request = ctx.request();
    HttpServerResponse response = ctx.response();
    if (!isAuthorizedUser(request)) {
      response.putHeader("location", "/").setStatusCode(401).end();
    }
    ctx.next();
  }

  public boolean isAuthorizedUser(HttpServerRequest request) {
    try {
      Claims c = auth.getClaims(request);
      if (c == null || isBlacklistedToken(c.getId()))
        return false;

      return c.getExpiration().getTime() > System.currentTimeMillis();
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isBlacklistedToken(String jti) {
    return processor.isBlacklistedToken(jti);
  }
}
