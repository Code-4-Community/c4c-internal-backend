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
import io.netty.handler.codec.http.HttpResponse;
import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

public class ApiRouter {
  private final IProcessor processor;
  private Vertx v;

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

    Router router = Router.router(vertx);
    v = vertx;
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    Route homeRoute = router.route("/");
    homeRoute.handler(this::handleHome);

    Route loginRoute = router.route("/login");
    loginRoute.handler(this::handleLogin);

    /*
     * after route is not needed as the only purpose of login is now to return the
     * JWT Route afterRoute = router.route("/after");
     * afterRoute.handler(this::handleAfter);
     */

    Route signUpRoute = router.route("/signup");
    signUpRoute.handler(this::handleSignUp);

    Route logoutRoute = router.route("/logout");
    logoutRoute.handler(this::handleLogout);

    /*
     * Session counter is no longer needed as we dont use sessions. Route
     * sessionRoute = router.route("/session");
     * sessionRoute.handler(this::handleSession);
     */

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
    response.putHeader("content-type", "application/json");
    response.end("<h1>go to /login with query string</h1>");
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
      response.setStatusCode(201).end();
    else {
      response.setStatusCode(400).end();
    }
  }

  private void handleLogin(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();

    if (isAuthorizedUser(request)) {
      response.putHeader("location", "/").setStatusCode(302).end();
    }
    String username = "";
    String password = "";

    if (request.query() != null && !(request.query().isEmpty())) {
      username = request.getParam("username");
      password = request.getParam("password");
    }

    if (processor.validate(username, password)) {

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

      String token = createJWT("login", username, "subject", TOKEN_DURATION);
      // JWT given back in header
      response.putHeader("Authorization", "Bearer " + token);
      // could respond with the token in body, but for now Authorization is passed
      // back in header
      response.setStatusCode(200).end();

    } else {
      response.putHeader("content-type", "text/json").setStatusCode(400).end();

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

  private void handleAttendMeeting(RoutingContext ctx) {
    checkAuthentication(ctx);

    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    String meetingid = "";
    String username = "";

    try {
      username = getClaims(request).getIssuer();
    } catch (Exception e) {
      username = "";
    }

    if (request.query() != null && !(request.query().isEmpty())) {
      MultiMap params = request.params();
      meetingid = params.get("id");
    }

    boolean success = false;

    if (!meetingid.equals("") && !username.equals(""))
      success = processor.attendMeeting(meetingid, username);

    if (success) {
      response.setStatusCode(201).end();
    } else {
      response.setStatusCode(400).end();
    }
  }

  public static String createJWT(String id, String issuer, String subject, long ttlMillis) {
    // The JWT signature algorithm we will be using to sign the token
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);

    // We will sign our JWT with our ApiKey secret
    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(JWT_KEY);
    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

    // Let's set the JWT Claims
    JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).setIssuer(issuer)
        .signWith(signatureAlgorithm, signingKey);

    // if it has been specified, let's add the expiration
    if (ttlMillis > 0) {
      long expMillis = nowMillis + ttlMillis;
      Date exp = new Date(expMillis);
      builder.setExpiration(exp);
    }
    // Builds the JWT and serializes it to a compact, URL-safe string
    return builder.compact();
  }

  private void checkAuthentication(RoutingContext ctx) {
    HttpServerRequest request = ctx.request();
    HttpServerResponse response = ctx.response();
    if (!isAuthorizedUser(request)) {
      response.putHeader("location", "/").setStatusCode(403).end();
      return;
    }
  }

  public boolean isAuthorizedUser(HttpServerRequest request) {
    try {
      return getClaims(request) != null;
    } catch (Exception e) {
      return false;
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
