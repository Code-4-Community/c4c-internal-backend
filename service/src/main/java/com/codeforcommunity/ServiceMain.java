package com.codeforcommunity;

import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.processor.ProcessorImpl;
import com.codeforcommunity.propertiesLoader.PropertiesLoader;
import com.codeforcommunity.rest.ApiRouter;
import com.codeforcommunity.util.JWTUtils;
import java.util.Properties;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public class ServiceMain {
  private DSLContext db;
  private final Properties dbProperties = PropertiesLoader.getDbProperties();

  public static void main(String[] args) {
    ServiceMain serviceMain = new ServiceMain();
    serviceMain.initialize();
  }

  /** Start the server, get everything going. */
  public void initialize() {
    connectDb();
    initializeServer();
  }

  /** Connect to the database and create a DSLContext so jOOQ can interact with it. */
  private void connectDb() {
    // This block ensures that the MySQL driver is loaded in the classpath
    try {
      Class.forName(dbProperties.getProperty("database.driver"));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    DSLContext db =
        DSL.using(
            dbProperties.getProperty("database.url"),
            dbProperties.getProperty("database.username"),
            dbProperties.getProperty("database.password"));
    this.db = db;
  }

  /** Initialize the server and get all the supporting classes going. */
  private void initializeServer() {
    IProcessor processor = new ProcessorImpl(this.db);
    JWTUtils auth =
        new JWTUtils(
            PropertiesLoader.getJwtProperties().getProperty("secret_key"),
            Integer.parseInt(
                PropertiesLoader.getExpirationProperties().getProperty("ms_access_expiration")));
    ApiRouter router = new ApiRouter(processor, auth);

    startApiServer(router);
  }

  /** Start up the actual API server that will listen for requests. */
  private void startApiServer(ApiRouter router) {
    ApiMain apiMain = new ApiMain(router);
    apiMain.startHttpsServerApi();
  }
}
