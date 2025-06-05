package io.akka.health.ingest.domain;

import akka.Done;
import io.akka.health.common.MongoDbUtils;
import io.akka.health.common.OpenAiUtils;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Index {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final OpenAiEmbeddingModel embeddingModel;
  private final MongoDbEmbeddingStore embeddingStore;
  private final DocumentSplitter splitter;

  public Index(MongoDbUtils.MongoDbConfig mongoDbConfig) {
    this.embeddingModel = OpenAiUtils.embeddingModel();
    this.embeddingStore = MongoDbUtils.mongoDbEmbeddingStore(mongoDbConfig);
    this.splitter = new DocumentByCharacterSplitter(500, 50);
  }

  public CompletionStage<Done> indexMedicalRecord(MedicalRecord medicalRecord) {
    Metadata metadata = Metadata.metadata("patientId", medicalRecord.patientId());
    metadata.put("reasonForVisit", medicalRecord.reasonForVisit());
    metadata.put("diagnosis", medicalRecord.diagnosis());
    Document document = Document.from(medicalRecord.toString(), metadata);
    List<TextSegment> segments = splitter.split(document);

    // ingest each segment
    var done = CompletableFuture.completedFuture(Done.getInstance());
    return segments.stream().reduce(
            done,
            (acc, seg) -> indexSegment(seg),
            (a,b) -> done);
  }

  private CompletableFuture<Done> indexSegment(TextSegment segment) {
    return CompletableFuture
            // embed the segment
            .supplyAsync(() -> embeddingModel.embed(segment))
            // store the embedding in MongoDB
            .thenCompose(res ->
                    CompletableFuture.supplyAsync(() -> embeddingStore.add(res.content(), segment)))
            .thenApply(__ -> Done.getInstance());
  }
}
