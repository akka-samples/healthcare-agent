package io.akka.health.common;


import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;

public class OpenAiUtils {

  final private static OpenAiEmbeddingModelName embeddingModelName = OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;

  public static OpenAiEmbeddingModel embeddingModel() {
    return OpenAiEmbeddingModel.builder()
      .apiKey(System.getenv("OPENAI_API_KEY"))
      .modelName(embeddingModelName)
      .build();
  }
}
