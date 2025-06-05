package io.akka.health.fitbit.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record class representing weight log data from Fitbit API.
 */
public record WeightLogData(
    @JsonProperty("weight") List<Weight> weight
) {
    /**
     * Record representing a weight entry.
     */
    public record Weight(
        @JsonProperty("bmi") Double bmi,
        @JsonProperty("date") LocalDate date,
        @JsonProperty("logId") Long logId,
        @JsonProperty("source") String source,
        @JsonProperty("time") String time,
        @JsonProperty("weight") Double weight,
        @JsonProperty("fat") Double fat
    ) {}
}