package com.codeforcommunity.rest;

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
import com.codeforcommunity.exceptions.MalformedParameterException;
import com.codeforcommunity.dto.ApplicantReturn;
import com.codeforcommunity.dto.NewsReturn;

import com.codeforcommunity.util.UpdatableBCrypt;
import com.codeforcommunity.util.JWTUtils;

import com.codeforcommunity.rest.subrouter.CommonRouter;
import com.codeforcommunity.rest.subrouter.EventsRouter;
import com.codeforcommunity.rest.subrouter.UsersAuthRouter;
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

public class ApiRouter {
  private final IProcessor processor;
  private Vertx v;

  private final JWTUtils auth;

  public ApiRouter(IProcessor processor, JWTUtils auth) {
    this.processor = processor;
    this.auth = auth;
  }

  /**
   * Initialize a router and register all route handlers on it.
   */
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);
    v = vertx;
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    router.route().handler(CorsHandler.create("*"));

    router.mountSubRouter("/", new CommonRouter(processor, auth).initializeRouter(vertx));

    router.mountSubRouter("/", new UsersAuthRouter(processor, auth).initializeRouter(vertx));

    router.mountSubRouter("/", new EventsRouter(processor, auth).initializeRouter(vertx));

    Route homeRoute = router.route("/");
    homeRoute.handler(this::handleHome);

    // Applicants

    Route getApplicantsRoute = router.get().path("/admin/applicants");
    getApplicantsRoute.handler(this::handleGetApplicants);

    Route createApplicantRoute = router.post("/protected/applicants");
    createApplicantRoute.handler(this::handleCreateApplicant);

    Route getApplicantRoute = router.get("/admin/applicants/:id");
    getApplicantRoute.handler(this::handleGetApplicant);

    Route updateApplicantRoute = router.put("/protected/applicants");
    updateApplicantRoute.handler(this::handleUpdateApplicant);

    Route deleteApplicantRoute = router.delete("/admin/applicants/:userid");
    deleteApplicantRoute.handler(this::handleDeleteApplicant);

    // News

    Route getAllNewsRoute = router.get("/news");
    getAllNewsRoute.handler(this::handleGetAllNews);

    Route createNewsRoute = router.post("/admin/news");
    createNewsRoute.handler(this::handleCreateNews);

    Route getNewsRoute = router.get("/news/:id");
    getNewsRoute.handler(this::handleGetNews);

    Route updateNewsRoute = router.put("/admin/news/:id");
    updateNewsRoute.handler(this::handleUpdateNews);

    Route deleteNewsRoute = router.delete("/admin/news/:id");
    deleteNewsRoute.handler(this::handleDeleteNews);

    return router;
  }

  private void handleHome(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("Content-Type", "text/html; charset=UTF-8");
    response
        .end("<a href=\"https://github.com/Code-4-Community/c4c-internal-backend/blob/master/api.md\">API Docs</a>");
  }

  private void handleGetApplicants(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");
    List<ApplicantReturn> applicants = processor.getAllApplicants();

    String applicantJson = null;
    try {
      applicantJson = JacksonMapper.getMapper().writeValueAsString(applicants);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }
    response.end(applicantJson);
  }

  private void handleCreateApplicant(RoutingContext ctx) {

    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();

    JsonObject body = ctx.getBodyAsJson();

    int userId = -1;
    byte[] fileBLOB = null;
    String fileType = "";
    String[] interests = null;
    String priorInvolvement = "";
    String whyJoin = "";

    try {
      userId = auth.getUserId(request);
      fileBLOB = body.getBinary("fileBLOB");
      fileType = body.getString("fileType");

      JsonArray interestsJSON = body.getJsonArray("interests");
      interests = new String[interestsJSON.size()];
      for (int i = 0; i < interests.length; i++) {
        interests[i] = interestsJSON.getString(i);
      }

      priorInvolvement = body.getString("priorInvolvement");
      whyJoin = body.getString("whyJoin");

    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    Optional<ApplicantReturn> ret = Optional.empty();
    if (userId != -1 && fileBLOB != null && fileType != null && interests != null && priorInvolvement != null
        && whyJoin != null)
      ret = processor.createApplicant(userId, fileBLOB, fileType, interests, priorInvolvement, whyJoin);

    String json = "";
    try {
      if (ret.isPresent())
        json = JacksonMapper.getMapper().writeValueAsString(ret.get());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    if (!json.isEmpty()) {
      response.setStatusCode(201).putHeader("content-type", "text/json").end(json);
    } else {
      response.setStatusCode(400).end();
    }
  }

  private void handleGetApplicant(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    int id = -1;

    try {
      id = Integer.parseInt(request.params().get("id"));
    } catch (NumberFormatException e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    Optional<ApplicantReturn> ret = processor.getApplicant(id);
    String json = "";
    try {
      if (ret.isPresent())
        json = JacksonMapper.getMapper().writeValueAsString(ret.get());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    if (!json.isEmpty()) {
      response.setStatusCode(200).putHeader("content-type", "text/json").end(json);
    } else {
      response.setStatusCode(400).end();
    }
  }

  private void handleUpdateApplicant(RoutingContext ctx) {
    try {

      HttpServerResponse response = ctx.response();
      HttpServerRequest request = ctx.request();

      JsonObject body = ctx.getBodyAsJson();

      int userId = -1;
      byte[] fileBLOB = null;
      String fileType = "";
      String[] interests = null;
      String priorInvolvement = "";
      String whyJoin = "";

      try {
        userId = auth.getUserId(request);
        fileBLOB = body.getBinary("fileBLOB");
        fileType = body.getString("fileType");

        JsonArray interestsJSON = body.getJsonArray("interests");
        interests = new String[interestsJSON.size()];
        for (int i = 0; i < interests.length; i++) {
          interests[i] = interestsJSON.getString(i);
        }

        priorInvolvement = body.getString("priorInvolvement");
        whyJoin = body.getString("whyJoin");

      } catch (Exception e) {
        e.printStackTrace();
        response.setStatusCode(400).end();
      }

      Optional<ApplicantReturn> ret = Optional.empty();
      if (userId != -1 && fileBLOB != null && fileType != null && interests != null && priorInvolvement != null
          && whyJoin != null)
        ret = processor.updateApplicant(userId, fileBLOB, fileType, interests, priorInvolvement, whyJoin);

      String json = "";
      try {
        if (ret.isPresent())
          json = JacksonMapper.getMapper().writeValueAsString(ret.get());
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }

      if (!json.isEmpty()) {
        response.setStatusCode(200).putHeader("content-type", "text/json").end(json);
      } else {
        response.setStatusCode(400).end();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleDeleteApplicant(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    int userId = -1;
    try {
      // userId = getUserId(request);
      userId = Integer.parseInt(request.params().get("userid"));
      String json = "";

      Optional<ApplicantReturn> ret = processor.deleteApplicant(userId);
      if (ret.isPresent())
        json = JacksonMapper.getMapper().writeValueAsString(ret.get());

      if (!json.isEmpty()) {
        response.setStatusCode(200).putHeader("content-type", "text/json").end(json);
      } else {
        response.setStatusCode(400).end();
      }
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }
  }

  private void handleGetAllNews(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");
    List<NewsReturn> news = processor.getAllNews();

    String newsJson = null;
    try {
      newsJson = JacksonMapper.getMapper().writeValueAsString(news);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    response.end(newsJson);
  }

  private void handleCreateNews(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    JsonObject body = ctx.getBodyAsJson();
    String title = "";
    String description = "";
    String imageUrl = "";
    String author = "";
    LocalDateTime date = null;
    String content = "";
    // for now, the input date is to the minute
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    try {
      title = body.getString("title");
      description = body.getString("description");
      imageUrl = body.getString("imageUrl");
      author = body.getString("author");
      date = LocalDateTime.parse(body.getString("date"), formatter);
      content = body.getString("content");
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }
    Optional<NewsReturn> ret = Optional.empty();
    if (title != null && description != null && imageUrl != null && author != null && date != null && content != null)
      ret = processor.createNews(title, description, imageUrl, author, date, content);

    String json = "";
    try {
      if (ret.isPresent())
        json = JacksonMapper.getMapper().writeValueAsString(ret.get());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    if (!json.isEmpty()) {
      response.setStatusCode(201).putHeader("content-type", "text/json").end(json);
    } else {
      response.setStatusCode(400).end();
    }
  }

  private void handleGetNews(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    int id = -1;

    try {
      id = Integer.parseInt(request.params().get("id"));
    } catch (NumberFormatException e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    Optional<NewsReturn> ret = processor.getNews(id);
    String json = "";
    try {
      if (ret.isPresent())
        json = JacksonMapper.getMapper().writeValueAsString(ret.get());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    if (!json.isEmpty()) {
      response.setStatusCode(200).putHeader("content-type", "text/json").end(json);
    } else {
      response.setStatusCode(400).end();
    }
  }

  private void handleUpdateNews(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    JsonObject body = ctx.getBodyAsJson();
    String title = "";
    String description = "";
    String imageUrl = "";
    String author = "";
    LocalDateTime date = null;
    String content = "";
    // for now, the input date is to the minute
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    int id = -1;

    try {
      id = Integer.parseInt(request.getParam("id"));
      title = body.getString("title");
      description = body.getString("description");
      imageUrl = body.getString("imageUrl");
      author = body.getString("author");
      date = LocalDateTime.parse(body.getString("date"), formatter);
      content = body.getString("content");
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    Optional<NewsReturn> ret = Optional.empty();
    if (title != null && description != null && imageUrl != null && author != null && date != null && content != null)
      ret = processor.updateNews(id, title, description, imageUrl, author, date, content);

    String json = "";
    try {
      if (ret.isPresent())
        json = JacksonMapper.getMapper().writeValueAsString(ret.get());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    if (!json.isEmpty()) {
      response.setStatusCode(200).putHeader("content-type", "text/json").end(json);
    } else {
      response.setStatusCode(400).end();
    }
  }

  private void handleDeleteNews(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    int id = -1;
    try {
      id = Integer.parseInt(request.params().get("id"));
      Optional<NewsReturn> ret = processor.deleteNews(id);
      String json = "";
      try {
        if (ret.isPresent())
          json = JacksonMapper.getMapper().writeValueAsString(ret.get());
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }

      if (!json.isEmpty()) {
        response.setStatusCode(200).putHeader("content-type", "text/json").end(json);
      } else {
        response.setStatusCode(400).end();
      }

    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }
  }

}
