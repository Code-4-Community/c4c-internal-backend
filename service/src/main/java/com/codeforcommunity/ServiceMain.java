package com.codeforcommunity;

import com.codeforcommunity.api.IProcessor;
import com.codeforcommunity.processor.ProcessorImpl;
import com.codeforcommunity.rest.ApiRouter;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public class ServiceMain {
  private DSLContext db;

  public static void main(String[] args) {
    ServiceMain serviceMain = new ServiceMain();
    serviceMain.initialize();
  }

  /**
   * Start the server, get everything going.
   */
  public void initialize() {
    connectDb();
    initializeServer();
  }

  /**
   * Connect to the database and create a DSLContext so jOOQ can interact with it.
   */
  private void connectDb() {
    //This block ensures that the MySQL driver is loaded in the classpath
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    //TODO: These arguments should be read out of a properties file
    DSLContext db = DSL.using("jdbc:postgresql://localhost:5432/checkin",
        "root", "apple");
    this.db = db;
  }

  /**
   * Initialize the server and get all the supporting classes going.
   */
  private void initializeServer() {
    IProcessor processor = new ProcessorImpl(this.db);
    ApiRouter router = new ApiRouter(processor);

    startApiServer(router);
  }

  /**
   * Start up the actual API server that will listen for requests.
   */
  private void startApiServer(ApiRouter router) {
    ApiMain apiMain = new ApiMain(router);
    apiMain.startApi();
  }
}
