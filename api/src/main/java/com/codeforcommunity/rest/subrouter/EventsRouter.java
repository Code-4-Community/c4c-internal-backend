package com.codeforcommunity.rest.subrouter;

import static com.codeforcommunity.rest.ApiRouter.end;

import com.codeforcommunity.JacksonMapper;
import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.dto.EventReturn;
import com.codeforcommunity.dto.UserReturn;
import com.codeforcommunity.util.JWTUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    end(ctx.response(), 200, eventsJson);
  }

  private void handleCreateEvent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
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
      end(ctx.response(), 400);
    }

    Optional<EventReturn> result = Optional.empty();
    if (name != null
        && date != null
        && subtitle != null
        && description != null
        && imageUrl != null
        && open != null
        && eventCode != null)
      result = processor.createEvent(name, subtitle, description, imageUrl, date, open, eventCode);

    if (result.isPresent()) {

      String json = "";
      try {
        if (result.isPresent()) json = JacksonMapper.getMapper().writeValueAsString(result.get());
      } catch (JsonProcessingException e) {
        end(ctx.response(), 400);
      }
      end(ctx.response(), 201, json);
    } else {
      end(ctx.response(), 400);
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
      end(ctx.response(), 400);
    }

    Optional<EventReturn> ret = processor.getEvent(id);
    String json = "";
    try {
      if (ret.isPresent()) json = JacksonMapper.getMapper().writeValueAsString(ret.get());
    } catch (JsonProcessingException e) {
    }

    if (!json.isEmpty()) {
      end(ctx.response(), 200, json);
    } else {
      end(ctx.response(), 400);
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
      end(ctx.response(), 400);
    }

    Optional<EventReturn> ret = Optional.empty();
    if (name != null
        && date != null
        && subtitle != null
        && description != null
        && imageUrl != null
        && open != null
        && eventCode != null)
      ret = processor.updateEvent(id, name, subtitle, description, imageUrl, date, open, eventCode);

    String json = "";
    try {
      if (ret.isPresent()) json = JacksonMapper.getMapper().writeValueAsString(ret.get());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    if (!json.isEmpty()) {
      end(ctx.response(), 200, json);
    } else {
      end(ctx.response(), 400);
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
      if (ret.isPresent()) json = JacksonMapper.getMapper().writeValueAsString(ret.get());

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
      end(ctx.response(), 400);
    }

    if (!eventCode.isEmpty() && userId != -1) success = processor.attendEvent(eventCode, userId);

    if (success) {
      end(ctx.response(), 200);
    } else {
      end(ctx.response(), 400);
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
      end(ctx.response(), 400);
    }
    String userJson = null;
    try {
      userJson = JacksonMapper.getMapper().writeValueAsString(users);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      end(ctx.response(), 400);
    }
    end(ctx.response(), 200, userJson);
  }
}
