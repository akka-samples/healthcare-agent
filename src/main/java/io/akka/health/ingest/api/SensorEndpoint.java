package io.akka.health.ingest.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.client.ComponentClient;
import io.akka.health.ingest.application.SensorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

/**
 * Endpoint to retrieve sensor data.
 */
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/sensor")
public class SensorEndpoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ComponentClient componentClient;

    public SensorEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    /**
     * Get all sensor data for a specific user.
     *
     * @param userId The unique identifier for the user
     * @return A CompletionStage containing all sensor data for the user
     */
    @Get("/{userId}")
    public CompletionStage<SensorView.AllSensorData> getSensorDataByUser(String userId) {
        logger.info("Retrieving sensor data for user: {}", userId);
        return componentClient.forView()
                .method(SensorView::getSensorDataByByUser)
                .invokeAsync(userId);
    }
}