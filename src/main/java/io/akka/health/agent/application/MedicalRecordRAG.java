package io.akka.health.agent.application;

import com.mongodb.client.MongoClient;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Metadata;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import io.akka.health.common.MongoDbUtils;
import io.akka.health.common.OpenAiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedicalRecordRAG {
    private final MongoDbUtils.MongoDbConfig mongoDbConfig;
    private final static Logger logger = LoggerFactory.getLogger(MedicalRecordRAG.class);


    public MedicalRecordRAG(MongoClient mongoClient) {
        this.mongoDbConfig = new MongoDbUtils.MongoDbConfig(
                mongoClient,
                "health",
                "medicalrecord",
                "medicalrecord-index");
    }

    public String retrieve(String question, String userId) {
        // Create a retriever
        var contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(MongoDbUtils.embeddingStore(mongoDbConfig))
                .embeddingModel(OpenAiUtils.embeddingModel())
                .maxResults(10)
                .minScore(0.1)
                // Currently the patientId must equal the userId
                .filter(MetadataFilterBuilder.metadataKey("patientId").isEqualTo(userId))
                .build();
        var retrievalAugmenter = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .build();

        // Retrieve the data
        var chatMessage = new UserMessage(question);
        var metadata = Metadata.from(chatMessage, null, null);
        var augmentationRequest = new AugmentationRequest(chatMessage, metadata);

        var result = retrievalAugmenter.augment(augmentationRequest);
        logger.info("Retrieved the following content: {}", result.contents());

        UserMessage augmented = (UserMessage) new DefaultContentInjector().inject(result.contents(), chatMessage);
        logger.info("Augmented message: {}", augmented);
        return augmented.singleText();
    }

}
