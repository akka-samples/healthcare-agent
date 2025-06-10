package io.akka.health;

import akka.javasdk.http.HttpClientProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import io.akka.health.agent.application.MedicalRecordRAG;
import io.akka.health.common.MongoDbUtils;
import io.akka.health.fitbit.FitbitClient;
import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setup
public class Bootstrap implements ServiceSetup {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final MedicalRecordRAG medicalRecordRAG;
  private final FitbitClient fitbitClient;
  private final MongoDbEmbeddingStore embeddingStore;

  public Bootstrap(HttpClientProvider httpClientProvider) {
    String mongodbAtlasUri = System.getenv("MONGODB_ATLAS_URI");
    if (mongodbAtlasUri == null) {
      logger.error("MONGODB_ATLAS_URI environment variable is not set.");
      throw new RuntimeException("MONGODB_ATLAS_URI environment variable is not set.");
    }

    String openaiApiKey = System.getenv("OPENAI_API_KEY");
    if (openaiApiKey == null) {
        logger.error("OPENAI_API_KEY environment variable is not set.");
      throw new RuntimeException("OPENAI_API_KEY environment variable is not set.");
    }

    String fitbitAccessToken = System.getenv("FITBIT_ACCESS_TOKEN");
    if (fitbitAccessToken == null) {
        logger.error("FITBIT_ACCESS_TOKEN environment variable is not set.");
      throw new RuntimeException("FITBIT_ACCESS_TOKEN environment variable is not set.");
    }

    var mongoClient = MongoClients.create(mongodbAtlasUri);
    var mongoConfig = new MongoDbUtils.MongoDbConfig(
            mongoClient,
            "health",
            "medicalrecord",
            "medicalrecord-index");
    this.embeddingStore = MongoDbUtils.mongoDbEmbeddingStore(mongoConfig);
    this.medicalRecordRAG = new MedicalRecordRAG(mongoClient);
    this.fitbitClient = new FitbitClient(httpClientProvider.httpClientFor("https://api.fitbit.com"));
  }

  @Override
  public DependencyProvider createDependencyProvider() {
    return new DependencyProvider() {
      @Override
      public <T> T getDependency(Class<T> cls) {
        if (cls.equals(MongoDbEmbeddingStore.class)) {
          return (T) embeddingStore;
        }

        if (cls.equals(MedicalRecordRAG.class)) {
          return (T) medicalRecordRAG;
        }

        if (cls.equals(FitbitClient.class)) {
            return (T) fitbitClient;
        }
        return null;
      }
    };
  }
}
