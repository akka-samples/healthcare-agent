package io.akka.health.ingest.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import io.akka.health.ingest.domain.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;


@ComponentId("sensor-entity")
public class SensorEntity extends EventSourcedEntity<SensorEntity.State, SensorEntity.Event> {

  public record State(String id, List<SensorData> data) {}
  public sealed interface Event {
    record Added(SensorData data) implements Event {}
  }

  private final String entityId;
  private static final Logger logger = LoggerFactory.getLogger(SensorEntity.class);

  public SensorEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public SensorEntity.State emptyState() {
    return new SensorEntity.State(entityId, List.of());
  }

  public Effect<Done> addData(SensorData data) {
    var event = new Event.Added(data);

    return effects()
        .persist(event)
        .thenReply(newState -> Done.getInstance());
  }

  public ReadOnlyEffect<SensorEntity.State> getState() {
    return effects().reply(currentState());
  }

  @Override
  public SensorEntity.State applyEvent(SensorEntity.Event event) {
    return switch (event) {
      case SensorEntity.Event.Added added -> {
        var newData = new java.util.ArrayList<>(currentState().data());
        newData.add(added.data);
        yield new SensorEntity.State(currentState().id(), newData);
      }
    };
  }
}
