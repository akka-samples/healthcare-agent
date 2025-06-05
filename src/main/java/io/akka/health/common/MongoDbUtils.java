package io.akka.health.common;

import com.mongodb.client.MongoClient;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;

public class MongoDbUtils {

  public record MongoDbConfig(
          MongoClient mongoClient,
          String databaseName,
          String collectionName,
          String indexName) {
  }

  public static EmbeddingStore<TextSegment> embeddingStore(MongoDbConfig conf) {
    return MongoDbEmbeddingStore.builder()
      .fromClient(conf.mongoClient)
      .databaseName(conf.databaseName)
      .collectionName(conf.collectionName)
      .indexName(conf.indexName)
      .createIndex(true)
      .build();
  }

  public static MongoDbEmbeddingStore mongoDbEmbeddingStore(MongoDbConfig conf) {
    return (MongoDbEmbeddingStore) embeddingStore(conf);
  }
}
