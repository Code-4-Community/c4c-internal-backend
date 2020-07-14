package com.codeforcommunity.propertiesLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

  private static Properties getProperties(String file) {
    try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(file)) {
      Properties prop = new Properties();
      prop.load(input);
      return prop;
    } catch (IOException ex) {
      throw new IllegalArgumentException("Cannot find file: " + file, ex);
    }
  }

  public static Properties getDbProperties() {
    return getProperties("properties/db.properties");
  }

  public static Properties getJwtProperties() {
    return getProperties("properties/jwt.properties");
  }

  public static Properties getExpirationProperties() {
    return getProperties("properties/expiration.properties");
  }
}
