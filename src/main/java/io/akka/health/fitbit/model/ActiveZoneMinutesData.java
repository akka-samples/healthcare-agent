package io.akka.health.fitbit.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * Record class representing active zone minutes data from Fitbit API.
 */
public record ActiveZoneMinutesData(
    @JsonProperty("activities-active-zone-minutes") List<DailyActiveZoneMinutes> activitiesActiveZoneMinutes,
    @JsonProperty("activities-active-zone-minutes-intraday") IntradayActiveZoneMinutes activitiesActiveZoneMinutesIntraday
) {
    /**
     * Record representing daily active zone minutes data.
     */
    public record DailyActiveZoneMinutes(
        @JsonProperty("dateTime") LocalDate dateTime,
        @JsonProperty("value") ActiveZoneMinutesValue value
    ) {}

    /**
     * Record representing active zone minutes value.
     */
    public record ActiveZoneMinutesValue(
        @JsonProperty("activeZoneMinutes") Integer activeZoneMinutes,
        @JsonProperty("fatBurnActiveZoneMinutes") Integer fatBurnActiveZoneMinutes,
        @JsonProperty("cardioActiveZoneMinutes") Integer cardioActiveZoneMinutes,
        @JsonProperty("peakActiveZoneMinutes") Integer peakActiveZoneMinutes
    ) {}

    /**
     * Record representing intraday active zone minutes data.
     */
    public record IntradayActiveZoneMinutes(
        @JsonProperty("dataset") List<ActiveZoneMinutesDataPoint> dataset,
        @JsonProperty("datasetInterval") Integer datasetInterval,
        @JsonProperty("datasetType") String datasetType
    ) {}

    /**
     * Record representing a single active zone minutes data point.
     */
    public record ActiveZoneMinutesDataPoint(
        @JsonProperty("time") String time,
        @JsonProperty("value") ActiveZoneMinutesValue value
    ) {}
}