package com.codeforcommunity.rest;

//import java.awt.RenderingHints.Key;
import java.sql.Date;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.codeforcommunity.JacksonMapper;
import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.dto.MemberReturn;
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
import io.jsonwebtoken.Claims;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;


public class ApiRouter {
  private final IProcessor processor;
  private Vertx v;

  public ApiRouter(IProcessor processor) {
    this.processor = processor;
  }

  /**
   * Initialize a router and register all route handlers on it.
   */
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);
    v = vertx;
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    Route homeRoute = router.route("/");
    homeRoute.handler(this::handleHome);

    Route loginRoute = router.route("/login");
    loginRoute.handler(this::handleLogin);

    Route afterRoute = router.route("/after");
    afterRoute.handler(this::handleAfter);

    Route signUpRoute = router.route("/signup");
    signUpRoute.handler(this::handleSignUp);

    Route logoutRoute = router.route("/logout");
    logoutRoute.handler(this::handleLogout);

    Route sessionRoute = router.route("/session");
    sessionRoute.handler(this::handleSession);

    Route getMemberRoute = router.route().path("/api/v1/members");
    getMemberRoute.handler(this::handleGetMemberRoute);

    Route createMeetingRoute = router.route("/protected/createmeeting");
    createMeetingRoute.handler(this::handleCreateMeeting);

    Route attendMeetingRoute = router.route("/protected/attendmeeting");
    attendMeetingRoute.handler(this::handleAttendMeeting);

    return router;
  }

  /**
   * Add a handler for getting all members.
   */
  private void handleGetMemberRoute(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");
    List<MemberReturn> members = processor.getAllMembers();

    String memberJson = null;
    try {
      memberJson = JacksonMapper.getMapper().writeValueAsString(members);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    response.end(memberJson);
  }

  /**
   * login handler
   */
  private void handleHome(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    Session session = ctx.session();
    if (session.isEmpty()) {
      session.put("count", 0);
      session.put("auth", 0);
    }

    response.putHeader("content-type", "application/json");
    response.end("<h1>go to /login with query string</h1>");
  }

  private void handleLogin(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    Session session = ctx.session();
    if (session.isEmpty()) {
      session.put("count", 0);
      session.put("auth", 0);
    } else if (session.get("auth").equals(1)) {
      response.putHeader("location", "/").setStatusCode(302).end();
      return;
    }
    HttpServerRequest request = ctx.request();
    String username = "";
    String password = "";

    if (request.query() != null && !(request.query().isEmpty())) {
      username = request.getParam("username");
      password = request.getParam("password");
    }
    if (processor.validate(username, password)) {
      ctx.session().put("auth", 1);

      processor.attendedMeeting(username);
//      JWTAuthOptions config = new JWTAuthOptions()
//          .setKeyStore(new KeyStoreOptions()
//              .setPath("C:\\Program Files\\Java\\jdk1.8.0_181\\bin\\keystore1.jks")
//              .setPassword("password"));
//
//      JWTAuth provider = JWTAuth.create(v, config);

      // on the verify endpoint once you verify the identity of the user by its username/password
//      String token = provider.generateToken(new JsonObject().put("User", username), new JWTOptions());
      // now for any request to protected resources you should pass this string in the HTTP header Authorization as:
      // Authorization: Bearer <token>
      String token = createJWT("login", username, "subject", 50000);
      response.putHeader("Authorization", "Bearer " + token);      
      ctx.session().put("username", username);
      response.putHeader("location", "/after").setStatusCode(302).end();
    } else {
      response.putHeader("content-type", "text/html");
      response.end("try again without " + username + " and " + password);
    }
  }

  private void handleAfter(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    if (!(ctx.session().isEmpty()) && ctx.session().get("auth").equals(1)) {
      response.putHeader("content-type", "text/html");
      response.end("<h1>logged in</h1>");
    } else {
      response.putHeader("content-type", "text/html");
      response.end("<h1>You need to log in to access this page</h1>");
    }
  }

  private void handleSignUp(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    String username = "";
    String password = "";

    if (request.query() != null && !(request.query().isEmpty())) {
      MultiMap params = request.params();
      username = params.get("username");
      password = params.get("password");
    }
    boolean success = false;
    if (!username.equals("") && !password.equals(""))
      success = processor.addMember(username, password);

    if (success)
      response.putHeader("location", "/login").setStatusCode(302).end();
    else {
      response.putHeader("content-type", "text/html");
      response.end("<h1>failed adding user, try again</h1>");
    }
  }

  private void handleSession(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();

    if (!(ctx.session().isEmpty()) && ctx.session().get("auth").equals(1)) {
      int count = ctx.session().get("count");
      count++;
      ctx.session().put("count", count);
      response.putHeader("content-type", "text/html");
      response.end("<h1>Session Count: " + ctx.session().get("count") + "</h1>");
    } else {
      response.putHeader("content-type", "text/html").end("<h1>Log In First</h1>");
    }
  }

  private void handleLogout(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();

    if (!(ctx.session().isEmpty()) && ctx.session().get("auth").equals(1)) {
      ctx.session().destroy();
      ctx.reroute(ctx.request().path());
    } else {
      response.putHeader("content-type", "text/html").end("<h1>Not Logged In</h1>");
    }
  }

  private void checkAuthentication(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();

    if (ctx.session().isEmpty() || !ctx.session().get("auth").equals(1)) {
      response.putHeader("location", "/").setStatusCode(403).end();
      return;
    }
  }

  private void handleCreateMeeting(RoutingContext ctx) {
    checkAuthentication(ctx);

    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    String id = "";
    String name = "";
    LocalDateTime date = null;
    Boolean open = null;
    // for now, the input date is to the minute
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    if (request.query() != null && !(request.query().isEmpty())) {
      MultiMap params = request.params();
      id = params.get("id");
      name = params.get("name");
      date = LocalDateTime.parse(params.get("date"), formatter);
      open = Boolean.parseBoolean(params.get("open"));
    }
    boolean success = false;
    if (!id.equals("") && !name.equals("") && !date.equals(null) && !open.equals(null))
      success = processor.createMeeting(id, name, date, open);

    if (success)
      response.setStatusCode(201).end();
    else {
      response.setStatusCode(400).end();
    }
  }

  public static String createJWT(String id, String issuer, String subject, long ttlMillis) {
    
    //The JWT signature algorithm we will be using to sign the token
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);

    //We will sign our JWT with our ApiKey secret
    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary("SECRET_KEY");
    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

    //Let's set the JWT Claims
    JwtBuilder builder = Jwts.builder().setId(id)
            .setIssuedAt(now)
            .setSubject(subject)
            .setIssuer(issuer)
            .signWith(signatureAlgorithm, signingKey);
  
    //if it has been specified, let's add the expiration
    if (ttlMillis > 0) {
        long expMillis = nowMillis + ttlMillis;
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);
    }  
  
    //Builds the JWT and serializes it to a compact, URL-safe string
    return builder.compact();
}


  private void handleAttendMeeting(RoutingContext ctx) {
    checkAuthentication(ctx);

    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    String meetingid = "";
    String username = ctx.session().get("username");

    if (request.query() != null && !(request.query().isEmpty())) {
      MultiMap params = request.params();
      meetingid = params.get("id");
    }
    boolean success = false;
    if (!meetingid.equals("") && !username.equals(""))
      success = processor.attendMeeting(meetingid, username);

    if (success)
      response.setStatusCode(201).end();
    else {
      response.setStatusCode(400).end();
    }
  }

}
