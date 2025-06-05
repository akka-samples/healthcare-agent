package io.akka.health;

import akka.javasdk.http.HttpClientProvider;
import io.akka.health.fitbit.FitbitClient;
import io.akka.health.common.KeyUtils;
import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import akka.javasdk.client.ComponentClient;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setup
public class Bootstrap implements ServiceSetup {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final MongoClient mongoClient;
  private final FitbitClient fitbitClient;

  public Bootstrap(ComponentClient componentClient, HttpClientProvider httpClientProvider, com.typesafe.config.Config config) {

    if (!KeyUtils.hasValidKeys()) {
      throw new IllegalStateException(
        "No API keys found. When running locally, make sure you have a " + ".env file located under " +
          "src/main/resources/ (see src/main/resources/.env.example). When running in production, " +
          "make sure you have OPENAI_API_KEY and MONGODB_ATLAS_URI defined as environment variable.");
    }

    this.mongoClient = MongoClients.create(KeyUtils.readMongoDbUri());
    this.fitbitClient = new FitbitClient(httpClientProvider.httpClientFor("https://api.fitbit.com"));
  }

  @Override
  public DependencyProvider createDependencyProvider() {
    return new DependencyProvider() {
      @Override
      public <T> T getDependency(Class<T> cls) {

        if (cls.equals(MongoClient.class)) {
          return (T) mongoClient;
        }

        if (cls.equals(FitbitClient.class)) {
            return (T) fitbitClient;
        }
        return null;
      }
    };
  }
}
