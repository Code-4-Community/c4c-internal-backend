package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IProcessor;

import java.util.HashMap;
import java.util.List;

import com.codeforcommunity.JacksonMapper;

import com.codeforcommunity.dto.UserReturn;
import com.codeforcommunity.exceptions.MalformedParameterException;

import com.codeforcommunity.util.UpdatableBCrypt;
import com.codeforcommunity.util.JWTUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.Optional;

import static com.codeforcommunity.rest.ApiRouter.end;

public class UsersAuthRouter {

  private HashMap<String, Integer> loginmap = new HashMap<String, Integer>();
  private static final UpdatableBCrypt bcrypt = new UpdatableBCrypt(12);
  private final IProcessor processor;
  private final JWTUtils auth;
  boolean flag = false;

  public UsersAuthRouter(IProcessor processor, JWTUtils auth) {
    this.processor = processor;
    this.auth = auth;
  }

  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

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
      end(ctx.response(), 400);
    }
    end(ctx.response(), 200, userJson);
  }

  /**
   * login handler
   */

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
      end(ctx.response(), 400);
    }

    if (!flag && processor.validate(email, password)) {
      UserReturn user = processor.getUserByEmail(email).get();

      String token = auth.createJWT("c4c", "auth-token", user.getId(), user.getPrivilegeLevel() > 0);
      response.putHeader("Authorization", "Bearer " + token);
      end(ctx.response(), 200);
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
      end(ctx.response(), 400);
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
      end(ctx.response(), 400);
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
      end(ctx.response(), 201, json);
    } else {
      end(ctx.response(), 400);
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
      end(ctx.response(), 200, json);
    } else {
      end(ctx.response(), 400);
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
      end(ctx.response(), 400);
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
      end(ctx.response(), 200, json);
    } else {
      end(ctx.response(), 400);
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
        end(ctx.response(), 200, json);
      } else {
        end(ctx.response(), 400);
      }
    } catch (Exception e) {
      e.printStackTrace();
      end(ctx.response(), 400);
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
      end(ctx.response(), 201);
    } else {
      // what do we do when logout fails? (secuirty risk)
      end(ctx.response(), 500);
    }
  }
}
