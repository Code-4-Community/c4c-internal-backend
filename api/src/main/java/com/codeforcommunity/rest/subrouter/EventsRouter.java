package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IProcessor;

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

public class EventsRouter {

  private final IProcessor processor;
  private final JWTUtils auth;

  public EventsRouter(IProcessor processor, JWTUtils auth) {
    this.processor = processor;
    this.auth = auth;
  }

  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    // Events

    Route getEventsRoute = router.get().path("/events");
    getEventsRoute.handler(this::handleGetEvents);

    Route createEventRoute = router.post("/admin/events");
    createEventRoute.handler(this::handleCreateEvent);

    Route getEventRoute = router.get("/events/:id");
    getEventRoute.handler(this::handleGetEvent);

    Route updateEventRoute = router.put("/admin/events/:id");
    updateEventRoute.handler(this::handleUpdateEvent);

    Route deleteEventRoute = router.delete("/admin/events/:id");
    deleteEventRoute.handler(this::handleDeleteEvent);

    // Event Check Ins

    Route attendEventRoute = router.post("/protected/eventcheckin/:code");
    attendEventRoute.handler(this::handleAttendEvent);

    Route getEventUsersRoute = router.get("/protected/eventcheckin/:id");
    getEventUsersRoute.handler(this::handleGetEventUsers);

    return router;
  }

  // Event Route Handlers
  private void handleGetEvents(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");
    List<EventReturn> events = processor.getAllEvents();

    String eventsJson = null;
    try {
      eventsJson = JacksonMapper.getMapper().writeValueAsString(events);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    response.end(eventsJson);
  }

  private void handleCreateEvent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    JsonObject body = ctx.getBodyAsJson();
    String name = "";
    String subtitle = "";
    String description = "";
    String imageUrl = "";
    LocalDateTime date = null;
    Boolean open = null;
    String eventCode = "";
    // for now, the input date is to the minute
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    try {
      name = body.getString("name");
      subtitle = body.getString("subtitle");
      description = body.getString("description");
      imageUrl = body.getString("imageUrl");
      date = LocalDateTime.parse(body.getString("date"), formatter);
      open = body.getBoolean("open");
      eventCode = body.getString("code");
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    Optional<EventReturn> result = Optional.empty();
    if (name != null && date != null && subtitle != null && description != null && imageUrl != null && open != null
        && eventCode != null)
      result = processor.createEvent(name, subtitle, description, imageUrl, date, open, eventCode);

    if (result.isPresent()) {

      String json = "";
      try {
        if (result.isPresent())
          json = JacksonMapper.getMapper().writeValueAsString(result.get());
      } catch (JsonProcessingException e) {
        response.setStatusCode(400).end();
      }
      response.setStatusCode(201).putHeader("content-type", "text/json").end(json);
    } else {
      response.setStatusCode(400).end();
    }

    response.setStatusCode(201);
  }

  private void handleGetEvent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    int id = -1;

    try {
      id = Integer.parseInt(request.params().get("id"));
    } catch (NumberFormatException e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    Optional<EventReturn> ret = processor.getEvent(id);
    String json = "";
    try {
      if (ret.isPresent())
        json = JacksonMapper.getMapper().writeValueAsString(ret.get());
    } catch (JsonProcessingException e) {
    }

    if (!json.isEmpty()) {
      response.setStatusCode(200).putHeader("content-type", "text/json").end(json);
    } else {
      response.setStatusCode(400).end();
    }
  }

  private void handleUpdateEvent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    JsonObject body = ctx.getBodyAsJson();
    String name = "";
    String subtitle = "";
    String description = "";
    String imageUrl = "";
    LocalDateTime date = null;
    Boolean open = null;
    String eventCode = "";
    // for now, the input date is to the minute
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    int id = -1;

    try {
      id = Integer.parseInt(request.getParam("id"));
      name = body.getString("name");
      subtitle = body.getString("subtitle");
      description = body.getString("description");
      imageUrl = body.getString("imageUrl");
      date = LocalDateTime.parse(body.getString("date"), formatter);
      open = body.getBoolean("open");
      eventCode = body.getString("code");
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    Optional<EventReturn> ret = Optional.empty();
    if (name != null && date != null && subtitle != null && description != null && imageUrl != null && open != null
        && eventCode != null)
      ret = processor.updateEvent(id, name, subtitle, description, imageUrl, date, open, eventCode);

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

  private void handleDeleteEvent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();

    int id = -1;
    try {
      id = Integer.parseInt(request.params().get("id"));
      String json = "";

      Optional<EventReturn> ret = processor.deleteEvent(id);
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

  private void handleAttendEvent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();

    String eventCode = "";
    int userId = -1;
    boolean success = false;

    try {
      userId = auth.getUserId(request);
      eventCode = request.params().get("code");
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    if (!eventCode.isEmpty() && userId != -1)
      success = processor.attendEvent(eventCode, userId);

    if (success) {
      response.setStatusCode(201).end();
    } else {
      response.setStatusCode(400).end();
    }
  }

  private void handleGetEventUsers(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();

    int eventId = -1;
    List<UserReturn> users = null;

    try {

      eventId = Integer.parseInt(request.params().get("id"));

      users = processor.getEventUsers(eventId);
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }
    String userJson = null;
    try {
      userJson = JacksonMapper.getMapper().writeValueAsString(users);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }
    response.end(userJson);
  }
}
