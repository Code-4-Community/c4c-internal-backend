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
  private HashMap<String, Integer> loginmap = new HashMap<String, Integer>();
  private final IProcessor processor;
  private Vertx v;
  boolean flag = false;

  private static final UpdatableBCrypt bcrypt = new UpdatableBCrypt(12);

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

    Route homeRoute = router.route("/");
    homeRoute.handler(this::handleHome);

    // allows handling of POST and DELETE routes.
    // router.route("/*").handler(BodyHandler.create());

    // Users

    Route loginRoute = router.post("/login");
    loginRoute.handler(this::handleLogin);

    Route signUpRoute = router.post("/signup");
    signUpRoute.handler(this::handleSignUp);

    Route getUsersRoute = router.get("/protected/users");
    getUsersRoute.handler(this::handleGetAllUsers);

    Route getUserRoute = router.get("/protected/users/:id");
    getUserRoute.handler(this::handleGetUser);

    Route updateUserRoute = router.put("/protected/users");
    updateUserRoute.handler(this::handleUpdateUser);

    Route deleteUserRoute = router.delete("/protected/users");
    deleteUserRoute.handler(this::handleDeleteUser);

    Route logoutRoute = router.route("/protected/logout");
    logoutRoute.handler(this::handleLogout);

    // Route sessionRoute = router.route("/session");
    // sessionRoute.handler(this::handleSession);

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

  /**
   * Add a handler for getting all users.
   */
  private void handleGetAllUsers(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");
    List<UserReturn> users = processor.getAllUsers();

    String userJson = null;
    try {
      userJson = JacksonMapper.getMapper().writeValueAsString(users);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }
    response.end(userJson);
  }

  /**
   * login handler
   */
  private void handleHome(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("Content-Type", "text/html; charset=UTF-8");
    response.end("<h1>go to /login with query string to login</h1>");
  }

  private void handleLogin(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();

    JsonObject body = ctx.getBodyAsJson();
    String email = "";
    String password = "";

    try {
      email = body.getString("email");
      password = body.getString("password");
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    if (!flag && processor.validate(email, password)) {
      UserReturn user = processor.getUserByEmail(email).get();

      String token = auth.createJWT("c4c", "auth-token", user.getId(), user.getPrivilegeLevel() > 0);
      response.putHeader("Authorization", "Bearer " + token);
      response.setStatusCode(200).end();
    }

    /*
     * Need to fix, because this will cause a timeout if login failure occurs 5
     * times in any period of time which could be with 1 minute or 2 weeks
     */
    else { // if user fails to input correct password
      if (loginmap.get(email) == null) { // first failure creates entry in cache
        loginmap.put(email, 0); // initializes to 0th failure
      } else
        loginmap.put(email, loginmap.get(email) + 1); // increments failure counter by 1
      if (loginmap.get(email) > 4) { // if they have failed 5 times
        // Block the user from logging in for 5 minutes
        flag = true;
        final String username1 = email;
        new Thread() {
          public void run() {
            try {
              Thread.sleep(300000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            flag = false;
            loginmap.put(username1, null); // initializes to 0th failure again after the 5 minutes is over
          }
        }.start();
      }

      response.putHeader("content-type", "text/json").setStatusCode(400).end();
    }
  }

  private void handleSignUp(RoutingContext ctx) {

    HttpServerResponse response = ctx.response();

    JsonObject body = ctx.getBodyAsJson();

    String email = "";
    String encryptedPassword = "";
    String firstName = "";
    String lastName = "";
    int currentYear = -1;
    String major = "";
    try {
      email = body.getString("email");
      firstName = body.getString("firstName");
      lastName = body.getString("lastName");
      encryptedPassword = bcrypt.hash(body.getString("password"));
      currentYear = body.getInteger("currentYear");
      major = body.getString("major");

    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    Optional<UserReturn> ret = Optional.empty();

    if (email != null && firstName != null && lastName != null && encryptedPassword != null && currentYear != -1
        && major != null)
      ret = processor.addUser(email, firstName, lastName, encryptedPassword, currentYear, major);

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

  private void handleGetUser(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    int id = -1;

    try {
      id = Integer.parseInt(request.params().get("id"));
    } catch (NumberFormatException e) {
      e.printStackTrace();
      throw new MalformedParameterException("id");
    }

    Optional<UserReturn> ret = processor.getUser(id);
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

  private void handleUpdateUser(RoutingContext ctx) {

    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();

    JsonObject body = ctx.getBodyAsJson();

    int id = -1;
    String email = "";
    String password = "";
    String encryptedPassword = "";

    String firstName = "";
    String lastName = "";

    int currentYear = -1;
    String major = "";
    try {
      id = auth.getUserId(request);
      email = body.getString("email");
      firstName = body.getString("firstName");
      lastName = body.getString("lastName");
      password = body.getString("password");

      currentYear = body.getInteger("currentYear");
      major = body.getString("major");
      if (!password.isEmpty())
        encryptedPassword = bcrypt.hash(password);
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }

    Optional<UserReturn> ret = Optional.empty();
    if (id != 0 && email != null && firstName != null && lastName != null && encryptedPassword != null
        && currentYear != -1 && major != null)
      ret = processor.updateUser(id, email, firstName, lastName, encryptedPassword, currentYear, major);

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

  private void handleDeleteUser(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    int id = -1;
    try {
      id = auth.getUserId(request);
      Optional<UserReturn> ret = processor.deleteUser(id);
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

  // handling logouts requires that we save state for some JWT, if we need to keep
  // track of state, why use stateless JWT? Wouldn't sessions be more appropriate?
  private void handleLogout(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    // onLogout clear expired tokens to periodically "trim" the blacklsited tokens
    // (should probably instead daily)
    boolean cleared = processor.clearBlacklistedTokens(auth.getDuration());
    boolean success = processor.addBlacklistedToken(auth.getClaims(request).getId());

    if (success) {
      // ctx.reroute(ctx.request().path());
      response.setStatusCode(201).end();
    } else {
      // what do we do when logout fails? (secuirty risk)
      response.setStatusCode(500).end();
    }
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
    // -----------------------DUPLICATE FOR OTHER TYPES------------------

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
    // ----------------------------------END------------------------------

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

    // -----------------------DUPLICATE FOR OTHER TYPES------------------

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
    // ----------------------------------END------------------------------
  }

  private void handleDeleteEvent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();

    // -----------------------DUPLICATE FOR OTHER TYPES------------------
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
    // ----------------------------------END------------------------------
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
