package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IProcessor;

import java.util.List;

import com.codeforcommunity.JacksonMapper;
import com.codeforcommunity.dto.ApplicantReturn;
import com.codeforcommunity.util.JWTUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.Optional;

public class ApplicantsRouter {

  private final IProcessor processor;
  private final JWTUtils auth;

  public ApplicantsRouter(IProcessor processor, JWTUtils auth) {
    this.processor = processor;
    this.auth = auth;
  }

  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

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

    return router;
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
}
