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
import com.codeforcommunity.util.UpdatableBCrypt;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
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

public class ApiRouter {
  private HashMap<String, Integer> loginmap = new HashMap<String, Integer>();
  private final IProcessor processor;
  private Vertx v;
  boolean flag = false;

  private static final UpdatableBCrypt bcrypt = new UpdatableBCrypt(12);

  // should really be saved as somewhere safer so that the secret isnt just laying
  // around on github
  private static final String JWT_KEY = "SECRET_KEY";

  // token duration is 60 minutes in milliseconds
  private static final long TOKEN_DURATION = 3600000;

  public ApiRouter(IProcessor processor) {
    this.processor = processor;
  }

  /**
   * Initialize a router and register all route handlers on it.
   */
  public Router initializeRouter(Vertx vertx) {
    System.out.println("got to router");
    Router router = Router.router(vertx);
    v = vertx;
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    Route homeRoute = router.route("/");
    homeRoute.handler(this::handleHome);

    // allows handling of POST and DELETE routes.
    router.route("/*").handler(BodyHandler.create());

    router.route("/protected/*").handler(this::checkAuthentication);
    router.route("/admin/*").handler(this::checkAdmin);

    Route loginRoute = router.post("/login");
    loginRoute.handler(this::handleLogin);

    /*
     * after route is not needed as the only purpose of login is now to return the
     * JWT Route afterRoute = router.route("/after");
     * afterRoute.handler(this::handleAfter);
     */

    // USER CRUD

    // Create User : Non-Protected POST method
    Route signUpRoute = router.post("/signup");
    signUpRoute.handler(this::handleSignUp);

    // Should any authorized user be able to see the information of another user?
    // For now, yes.
    // Get User : Protected POST method
    // Route getUserRoute = router.route("/protected/user/:id");
    // getUserRoute.handler(this::handleGetUser);

    // Route editUserRoute = router.post("/protected/user/:id");
    // editUserRoute.handler(this::handleEditUser);

    // Route deleteUserRoute = router.delete("/protected/user/:id");
    // deleteUserRoute.handler(this::handleDeleteUser);

    // should logout be a POST or GET? we dont supply information but it is
    // modifying a resource...
    // 200 or 201
    Route logoutRoute = router.route("/logout");
    logoutRoute.handler(this::handleLogout);

    // Route sessionRoute = router.route("/session");
    // sessionRoute.handler(this::handleSession);

    Route getUserRoute = router.route().path("/protected/users");
    getUserRoute.handler(this::handleGetUserRoute);

    Route getEventsRoute = router.route().path("/protected/events");
    getEventsRoute.handler(this::handleGetEvents);

    Route createEventRoute = router.post("/admin/event");
    createEventRoute.handler(this::handleCreateEvent);

    Route getEventRoute = router.get("/protected/event/:id");
    getEventRoute.handler(this::handleGetEvent);

    Route updateEventRoute = router.put("/admin/event/:id");
    updateEventRoute.handler(this::handleUpdateEvent);

    Route deleteEventRoute = router.delete("/admin/event/:id");
    deleteEventRoute.handler(this::handleDeleteEvent);

    // asd asdsad

    Route attendEventRoute = router.post("/protected/eventcheckin/:id");
    attendEventRoute.handler(this::handleAttendEvent);

    return router;
  }

  /**
   * Add a handler for getting all users.
   */
  private void handleGetUserRoute(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");
    List<UserReturn> users = processor.getAllUsers();

    String userJson = null;
    try {
      userJson = JacksonMapper.getMapper().writeValueAsString(users);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    response.end(userJson);
  }

  /**
   * login handler
   */
  private void handleHome(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");
    response.end("<h1>go to /login with query string</h1>");
  }

  private void handleLogin(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();

    if (isAuthorizedUser(request)) {
      response.putHeader("location", "/").setStatusCode(302).end();
    }
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

    System.out.println("got here");

    if (!flag && processor.validate(email, password)) {
      System.out.println("got past validation");

      // JWTAuthOptions config = new JWTAuthOptions()
      // .setKeyStore(new KeyStoreOptions()
      // .setPath("C:\\Program Files\\Java\\jdk1.8.0_181\\bin\\keystore1.jks")
      // .setPassword("password"));
      //
      // JWTAuth provider = JWTAuth.create(v, config);

      // on the verify endpoint once you verify the identity of the user by its
      // username/password
      // String token = provider.generateToken(new JsonObject().put("User", username),
      // new JWTOptions());
      // now for any request to protected resources you should pass this string in the
      // HTTP header Authorization as:
      // Authorization: Bearer <token>

      UserReturn user = processor.getUserByEmail(email);
      System.out.println(user);

      // something is terribly wrong with this JWT, the fields arent right
      String token = createJWT("c4c", "auth-token", TOKEN_DURATION, user.id, user.privilegeLevel > 0);
      // JWT given back in header
      response.putHeader("Authorization", "Bearer " + token);
      // could respond with the token in body, but for now Authorization is passed
      // back in header
      response.setStatusCode(200).end();
      // we COULD store misc info about the user in the session but
      // ctx.session().put("username", username);
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
    try {

      HttpServerResponse response = ctx.response();
      HttpServerRequest request = ctx.request();

      JsonObject body = ctx.getBodyAsJson();

      String email = "";
      String encryptedPassword = "";
      String firstName = "";
      String lastName = "";
      try {
        email = body.getString("email");
        firstName = body.getString("firstName");
        lastName = body.getString("lastName");
        encryptedPassword = bcrypt.hash(body.getString("password"));
      } catch (Exception e) {
        e.printStackTrace();
        response.setStatusCode(400).end();
      }
      System.out.println("got past variable setting");

      boolean success = false;
      if (!email.equals("") && !firstName.equals("") && !lastName.equals("") && !encryptedPassword.equals(""))
        success = processor.addUser(email, firstName, lastName, encryptedPassword);
      System.out.println("got past database addition");
      if (success)
        response.setStatusCode(201).end();
      else {
        response.setStatusCode(400).end();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // handling logouts requires that we save state for some JWT, if we need to keep
  // track of state, why use stateless JWT? Wouldn't sessions be more appropriate?
  private void handleLogout(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    if (isAuthorizedUser(request)) {
      // onLogout clear expired tokens to periodically "trim" the blacklsited tokens
      // (should probably instead daily)
      boolean cleared = processor.clearBlacklistedTokens(TOKEN_DURATION);
      boolean success = processor.addBlacklistedToken(request.headers().get("Authorization"));

      if (success) {
        // ctx.reroute(ctx.request().path());
        response.setStatusCode(201).end();
      } else {
        // what do we do when logout fails? (secuirty risk)
        response.setStatusCode(500).end();
      }

    } else {
      response.setStatusCode(400).end();
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
    LocalDateTime date = null;
    Boolean open = null;
    String code = "";
    // for now, the input date is to the minute
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    try {
      name = body.getString("name");
      date = LocalDateTime.parse(body.getString("date"), formatter);
      open = body.getBoolean("open");
      code = body.getString("code");
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }
    boolean success = false;
    if (!name.equals("") && !date.equals(null) && !open.equals(null))
      success = processor.createEvent(name, date, open, code);

    if (success)
      response.setStatusCode(201).end();
    else {
      response.setStatusCode(400).end();
    }

    response.setStatusCode(201);
  }

  private void handleGetEvent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    int id = -1;

    try{
    id = Integer.parseInt(request.params().get("id"));
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }
    EventReturn result = null;
    if (id > 0)
      result = processor.getEvent(id);

    System.out.println(result);

    String json = "";
    try {
      json = JacksonMapper.getMapper().writeValueAsString(result);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (result != null && !json.isEmpty()) {
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
    LocalDateTime date = null;
    Boolean open = null;
    String code = "";
    // for now, the input date is to the minute
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    int id = -1;

    try {
      id = Integer.parseInt(request.getParam("id"));
      name = body.getString("name");
      date = LocalDateTime.parse(body.getString("date"), formatter);
      open = body.getBoolean("open");
      code = body.getString("code");
    } catch (Exception e) {
      e.printStackTrace();
      response.setStatusCode(400).end();
    }
    boolean success = false;
    if (!name.equals("") && !date.equals(null) && !open.equals(null))
      success = processor.updateEvent(id, name, date, open, code);

    if (success)
      response.setStatusCode(201).end();
    else {
      response.setStatusCode(400).end();
    }

    response.setStatusCode(201);
  }

  private void handleDeleteEvent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    int id = -1;

    if (request.query() != null && !(request.query().isEmpty())) {
      MultiMap params = request.params();
      id = Integer.parseInt(params.get("id"));
    }

    try {
      processor.deleteEvent(id);
      response.setStatusCode(200).end();

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
      userId = getUserId(request);
      eventCode = request.params().get("id");
      System.out.println("parsed the event id successfully");
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

  public static String createJWT(String issuer, String subject, long ttlMillis, int userId, boolean isAdmin) {
    // The JWT signature algorithm we will be using to sign the token
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);

    // We will sign our JWT with our ApiKey secret
    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(JWT_KEY);
    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

    // Let's set the JWT Claims
    JwtBuilder builder = Jwts.builder().setId(UUID.randomUUID().toString()).setIssuedAt(now).setSubject(subject)
        .setIssuer(issuer).claim("userId", userId).claim("isAdmin", isAdmin).signWith(signatureAlgorithm, signingKey);

    // if it has been specified, let's add the expiration
    if (ttlMillis > 0) {
      long expMillis = nowMillis + ttlMillis;
      Date exp = new Date(expMillis);
      builder.setExpiration(exp);
    }
    // Builds the JWT and serializes it to a compact, URL-safe string
    return builder.compact();
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
      return (boolean) getClaims(request).get("isAdmin");
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
      return getClaims(request) != null;
    } catch (Exception e) {
      return false;
    }
  }

  public int getUserId(HttpServerRequest request) {
    try {
      return (int) getClaims(request).get("userId");
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  public Claims getClaims(HttpServerRequest request) {
    String jwt = request.headers().get("Authorization");
    boolean isNullOrEmpty = jwt == null || jwt.isEmpty();
    boolean isBlacklisted = isBlacklistedToken(jwt);
    if (isNullOrEmpty || isBlacklisted)
      return null;

    Claims c = decodeJWT(jwt.split(" ")[1]);
    return c;
  }

  public boolean isBlacklistedToken(String jwt) {
    return processor.isBlacklistedToken(jwt);
  }

  public Claims decodeJWT(String jwt) {
    try {
      return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(JWT_KEY)).parseClaimsJws(jwt).getBody();
    } catch (Exception e) {
      return null;
    }
  }
}
