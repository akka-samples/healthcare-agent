package io.akka.health.ingest.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.annotations.Table;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import io.akka.health.ingest.domain.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

@ComponentId("sensor-view")
public class SensorView extends View {

    private final static Logger logger = LoggerFactory.getLogger(SensorView.class);

    public record AllSensorData(List<SensorData> data) {}

    @Query("SELECT collect(*) as data FROM sensordata WHERE userId = :userId")
    public QueryEffect<AllSensorData> getSensorDataByByUser(String userId) {
        return queryResult();
    }

    @Table("sensordata")
    @Consume.FromEventSourcedEntity(SensorEntity.class)
    public static class SensorUpdater extends TableUpdater<SensorData> {
        public Effect<SensorData> onEvent(SensorEntity.Event event) {
            return switch (event) {
                case SensorEntity.Event.Added added -> {
                    yield effects().updateRow(added.data());
                }
            };
        }
    }
}
