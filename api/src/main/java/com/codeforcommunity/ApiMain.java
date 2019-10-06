package com.codeforcommunity;

import com.codeforcommunity.rest.ApiRouter;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

/**
 * The main point for the API.
 */
public class ApiMain {
  private final ApiRouter apiRouter;

  public ApiMain(ApiRouter apiRouter) {
    this.apiRouter = apiRouter;
  }

  /**
   * Start the API to start listening on a port.
   */
  public void startApi() {
    Vertx vertx = Vertx.vertx();
    HttpServer server = vertx.createHttpServer();

    Router router = apiRouter.initializeRouter(vertx);

    server.requestHandler(router).listen(8081);
  }
}
