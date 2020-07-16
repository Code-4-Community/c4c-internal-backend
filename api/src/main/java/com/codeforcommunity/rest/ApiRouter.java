package com.codeforcommunity.rest;

import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.rest.subrouter.ApplicantsRouter;
import com.codeforcommunity.rest.subrouter.CommonRouter;
import com.codeforcommunity.rest.subrouter.EventsRouter;
import com.codeforcommunity.rest.subrouter.NewsRouter;
import com.codeforcommunity.rest.subrouter.UsersAuthRouter;
import com.codeforcommunity.util.JWTUtils;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class ApiRouter {
  private final IProcessor processor;
  private Vertx v;

  private final JWTUtils auth;

  public ApiRouter(IProcessor processor, JWTUtils auth) {
    this.processor = processor;
    this.auth = auth;
  }

  /** Initialize a router and register all route handlers on it. */
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);
    v = vertx;
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    router.route().handler(CorsHandler.create("*"));

    router.mountSubRouter("/", new CommonRouter(processor, auth).initializeRouter(vertx));

    router.mountSubRouter("/", new UsersAuthRouter(processor, auth).initializeRouter(vertx));

    router.mountSubRouter("/", new EventsRouter(processor, auth).initializeRouter(vertx));

    router.mountSubRouter("/", new ApplicantsRouter(processor, auth).initializeRouter(vertx));

    router.mountSubRouter("/", new NewsRouter(processor).initializeRouter(vertx));

    Route homeRoute = router.route("/");
    homeRoute.handler(this::handleHome);

    return router;
  }

  private void handleHome(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("Content-Type", "text/html; charset=UTF-8");
    response.end(
        "<a href=\"https://github.com/Code-4-Community/c4c-internal-backend/blob/master/api.md\">API Docs</a>");
  }

  public static void end(HttpServerResponse response, int statusCode) {
    end(response, statusCode, null);
  }

  public static void end(HttpServerResponse response, int statusCode, String jsonBody) {
    response
        .setStatusCode(statusCode)
        .putHeader("Content-Type", "application/json")
        .putHeader("Access-Control-Allow-Origin", "*")
        .putHeader("Access-Control-Allow-Methods", "DELETE, POST, GET, OPTIONS")
        .putHeader(
            "Access-Control-Allow-Headers",
            "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    if (jsonBody == null || jsonBody.equals("")) {
      response.end();
    } else {
      response.end(jsonBody);
    }
  }
}
