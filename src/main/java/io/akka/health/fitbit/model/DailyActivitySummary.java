package io.akka.health.fitbit.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record class representing daily activity summary data from Fitbit API.
 */
public record DailyActivitySummary(
    @JsonProperty("activities") List<Activity> activities,
    @JsonProperty("goals") Goals goals,
    @JsonProperty("summary") Summary summary
) {
    /**
     * Record representing an activity.
     */
    public record Activity(
        @JsonProperty("activityId") Long activityId,
        @JsonProperty("activityParentId") Long activityParentId,
        @JsonProperty("activityParentName") String activityParentName,
        @JsonProperty("calories") Integer calories,
        @JsonProperty("description") String description,
        @JsonProperty("duration") Long duration,
        @JsonProperty("hasActiveZoneMinutes") Boolean hasActiveZoneMinutes,
        @JsonProperty("hasStartTime") Boolean hasStartTime,
        @JsonProperty("isFavorite") Boolean isFavorite,
        @JsonProperty("lastModified") LocalDateTime lastModified,
        @JsonProperty("logId") Long logId,
        @JsonProperty("name") String name,
        @JsonProperty("startDate") LocalDate startDate,
        @JsonProperty("startTime") String startTime,
        @JsonProperty("steps") Integer steps
    ) {}

    /**
     * Record representing activity goals.
     */
    public record Goals(
        @JsonProperty("activeMinutes") Integer activeMinutes,
        @JsonProperty("caloriesOut") Integer caloriesOut,
        @JsonProperty("distance") Double distance,
        @JsonProperty("floors") Integer floors,
        @JsonProperty("steps") Integer steps
    ) {}

    /**
     * Record representing activity summary.
     */
    public record Summary(
        @JsonProperty("activeScore") Integer activeScore,
        @JsonProperty("activityCalories") Integer activityCalories,
        @JsonProperty("caloriesBMR") Integer caloriesBMR,
        @JsonProperty("caloriesOut") Integer caloriesOut,
        @JsonProperty("distances") List<Distance> distances,
        @JsonProperty("elevation") Double elevation,
        @JsonProperty("fairlyActiveMinutes") Integer fairlyActiveMinutes,
        @JsonProperty("floors") Integer floors,
        @JsonProperty("heartRateZones") List<HeartRateData.HeartRateZone> heartRateZones,
        @JsonProperty("lightlyActiveMinutes") Integer lightlyActiveMinutes,
        @JsonProperty("marginalCalories") Integer marginalCalories,
        @JsonProperty("restingHeartRate") Integer restingHeartRate,
        @JsonProperty("sedentaryMinutes") Integer sedentaryMinutes,
        @JsonProperty("steps") Integer steps,
        @JsonProperty("veryActiveMinutes") Integer veryActiveMinutes
    ) {}

    /**
     * Record representing a distance entry.
     */
    public record Distance(
        @JsonProperty("activity") String activity,
        @JsonProperty("distance") Double distance
    ) {}
}