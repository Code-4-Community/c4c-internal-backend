package com.codeforcommunity;

import com.codeforcommunity.rest.ApiRouter;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
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
    HttpServerOptions options = new HttpServerOptions()
        .addEnabledSecureTransportProtocol("TLSv1.2")
        .setSsl(true)
        .setKeyStoreOptions(new JksOptions()
            .setPath("keystore.jks")
            .setPassword("password"));
    HttpServer server = vertx.createHttpServer(options);

    Router router = apiRouter.initializeRouter(vertx);

    server.requestHandler(router).listen(8443);
  }

  public void startHTTP() {
    Vertx vertx = Vertx.vertx();
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);

    //Start
    server
        .requestHandler(r -> {
          r.response()
              .setStatusCode(301)
              .putHeader("Location", r.absoluteURI().replace("http", "https").replace("8081", "8443"))
              .end();
        }).listen(8081);

  }
}
