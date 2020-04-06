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
  private final ApiRouter apiRouter;
  private final Properties serverProperties = new Properties();

  public ApiMain(ApiRouter apiRouter) {
    this.apiRouter = apiRouter;
    loadProperties();

  }

  /**
   * Load properties from a server.properties file into a Properties field.
   */
  private void loadProperties() {
    InputStream propertiesStream = this.getClass().getClassLoader().getResourceAsStream("server.properties");
    try {
      serverProperties.load(propertiesStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Start the API to start listening on a port.
   */
  public void startHttpsServerApi() {
    Vertx vertx = Vertx.vertx();

    HttpServerOptions serverOptions = new HttpServerOptions();

    HttpServer server = vertx.createHttpServer(serverOptions);
    Router router = apiRouter.initializeRouter(vertx);
    System.out.println("Server listening on port " + serverProperties.getProperty("server.port"));
    server.requestHandler(router).listen(Integer.parseInt(serverProperties.getProperty("server.port")));
  }
}
