package com.codeforcommunity.rest.subrouter;

//import java.awt.RenderingHints.Key;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.login.LoginContext;
import javax.xml.bind.DatatypeConverter;

import com.codeforcommunity.JacksonMapper;
import com.codeforcommunity.api.IProcessor;

import com.codeforcommunity.dto.EventReturn;
import com.codeforcommunity.dto.UserReturn;
import com.codeforcommunity.dto.ApplicantReturn;
import com.codeforcommunity.dto.NewsReturn;
import com.codeforcommunity.util.JWTUtils;
import com.codeforcommunity.util.UpdatableBCrypt;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.handler.CorsHandler;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;

import io.jsonwebtoken.*;

//import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.netty.handler.codec.http.HttpResponse;
import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.json.Json;
import java.util.UUID;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Optional;

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
