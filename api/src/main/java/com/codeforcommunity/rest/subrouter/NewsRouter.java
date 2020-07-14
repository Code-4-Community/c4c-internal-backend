package com.codeforcommunity.rest.subrouter;

import static com.codeforcommunity.rest.ApiRouter.end;

import com.codeforcommunity.JacksonMapper;
import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.dto.NewsReturn;
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

public class NewsRouter {

  private final IProcessor processor;

  public NewsRouter(IProcessor processor) {
    this.processor = processor;
  }

  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

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
    end(ctx.response(), 200, newsJson);
  }

  private void handleCreateNews(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
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
      end(ctx.response(), 400);
    }
    Optional<NewsReturn> ret = Optional.empty();
    if (title != null
        && description != null
        && imageUrl != null
        && author != null
        && date != null
        && content != null)
      ret = processor.createNews(title, description, imageUrl, author, date, content);

    String json = "";
    try {
      if (ret.isPresent()) json = JacksonMapper.getMapper().writeValueAsString(ret.get());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    if (!json.isEmpty()) {
      end(ctx.response(), 201, json);
    } else {
      end(ctx.response(), 400);
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
      end(ctx.response(), 400);
    }

    Optional<NewsReturn> ret = processor.getNews(id);
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
      end(ctx.response(), 400);
    }

    Optional<NewsReturn> ret = Optional.empty();
    if (title != null
        && description != null
        && imageUrl != null
        && author != null
        && date != null
        && content != null)
      ret = processor.updateNews(id, title, description, imageUrl, author, date, content);

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

  private void handleDeleteNews(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    HttpServerRequest request = ctx.request();
    int id = -1;
    try {
      id = Integer.parseInt(request.params().get("id"));
      Optional<NewsReturn> ret = processor.deleteNews(id);
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

    } catch (Exception e) {
      e.printStackTrace();
      end(ctx.response(), 400);
    }
  }
}
