package io.akka.health.fitbit.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * Record class representing heart rate data from Fitbit API.
 */
public record HeartRateData(
    @JsonProperty("activities-heart") List<DailyHeartRate> activitiesHeart,
    @JsonProperty("activities-heart-intraday") IntradayHeartRate activitiesHeartIntraday
) {
    /**
     * Record representing daily heart rate data.
     */
    public record DailyHeartRate(
        @JsonProperty("dateTime") LocalDate dateTime,
        @JsonProperty("value") HeartRateValue value
    ) {}

    /**
     * Record representing heart rate value with zones and resting heart rate.
     */
    public record HeartRateValue(
        @JsonProperty("customHeartRateZones") List<HeartRateZone> customHeartRateZones,
        @JsonProperty("heartRateZones") List<HeartRateZone> heartRateZones,
        @JsonProperty("restingHeartRate") Integer restingHeartRate
    ) {}

    /**
     * Record representing a heart rate zone.
     */
    public record HeartRateZone(
        @JsonProperty("caloriesOut") Double caloriesOut,
        @JsonProperty("max") Integer max,
        @JsonProperty("min") Integer min,
        @JsonProperty("minutes") Integer minutes,
        @JsonProperty("name") String name
    ) {}

    /**
     * Record representing intraday heart rate data.
     */
    public record IntradayHeartRate(
        @JsonProperty("dataset") List<HeartRateDataPoint> dataset,
        @JsonProperty("datasetInterval") Integer datasetInterval,
        @JsonProperty("datasetType") String datasetType
    ) {}

    /**
     * Record representing a single heart rate data point.
     */
    public record HeartRateDataPoint(
        @JsonProperty("time") String time,
        @JsonProperty("value") Integer value
    ) {}
}