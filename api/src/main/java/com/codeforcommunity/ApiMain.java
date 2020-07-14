package com.codeforcommunity;

import com.codeforcommunity.rest.ApiRouter;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The main point for the API.
 */
public class ApiMain {
  private static final int SERVER_PORT = 8081;
  private final ApiRouter apiRouter;

  public ApiMain(ApiRouter apiRouter) {
    this.apiRouter = apiRouter;
  }

  /**
   * Start the API to start listening on a port.
   */
  public void startHttpsServerApi() {
    Vertx vertx = Vertx.vertx();

    HttpServerOptions serverOptions = new HttpServerOptions();

    HttpServer server = vertx.createHttpServer(serverOptions);
    Router router = apiRouter.initializeRouter(vertx);

    System.out.println("Server listening on port " + SERVER_PORT);
    server.requestHandler(router).listen(SERVER_PORT);
  }
}
