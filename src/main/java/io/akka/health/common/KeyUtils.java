package io.akka.health.common;

import io.akka.health.Bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KeyUtils {

  public static String readMongoDbUri() {
    return readKey("MONGODB_ATLAS_URI");
  }

  public static String readOpenAiKey() {
    return readKey("OPENAI_API_KEY");
  }

  public static String readFitbitClientId() {
    return readKey("FITBIT_CLIENT_ID");
  }

  public static String readFitbitClientSecret() {
    return readKey("FITBIT_CLIENT_SECRET");
  }

  public static String readFitbitAccessToken() {
    return readKey("FITBIT_ACCESS_TOKEN");
  }

  public static boolean hasFitbitKeys() {
    try {
      return !readFitbitClientId().isEmpty() && !readFitbitClientSecret().isEmpty();
    } catch (Exception e) {
      return false;
    }
  }

  public static boolean hasFitbitAccessToken() {
    try {
      return !readFitbitAccessToken().isEmpty();
    } catch (Exception e) {
      return false;
    }
  }

  public static boolean hasValidKeys() {
    try {
      return !readMongoDbUri().isEmpty() && !readOpenAiKey().isEmpty();
    } catch (Exception e) {
      return false;
    }
  }

  private static String readKey(String key) {

    // first read from env var
    var value = System.getenv(key);

    // if not available, read from src/main/resources/.env file
    if (value == null) {
      var properties = new Properties();

      try (InputStream in = Bootstrap.class.getClassLoader().getResourceAsStream(".env")) {

        if (in == null) throw new IllegalStateException("No .env file found");
        else properties.load(in);

        return properties.getProperty(key);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return value;
  }
}
