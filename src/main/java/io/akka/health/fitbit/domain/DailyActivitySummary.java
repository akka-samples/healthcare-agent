package io.akka.health.fitbit.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record class representing daily activity summary data from Fitbit API.
 */
public record DailyActivitySummary(
        List<Activity> activities,
        Goals goals,
        Summary summary
) {


    /**
     * Record representing an activity.
     */
    public record Activity(
            Long activityId,
            Long activityParentId,
            String activityParentName,
            Integer calories,
            String description,
            Long duration,
            Boolean hasActiveZoneMinutes,
            Boolean hasStartTime,
            Boolean isFavorite,
            LocalDateTime lastModified,
            Long logId,
            String name,
            LocalDate startDate,
            String startTime,
            Integer steps
    ) {
    }

    /**
     * Record representing activity goals.
     */
    public record Goals(
            Integer activeMinutes,
            Integer caloriesOut,
            Double distance,
            Integer floors,
            Integer steps
    ) {
    }

    /**
     * Record representing activity summary.
     */
    public record Summary(
            Integer activeScore,
            Integer activityCalories,
            Integer caloriesBMR,
            Integer caloriesOut,
            List<Distance> distances,
            Double elevation,
            Integer fairlyActiveMinutes,
            Integer floors,
            List<HeartRateData.HeartRateZone> heartRateZones,
            Integer lightlyActiveMinutes,
            Integer marginalCalories,
            Integer restingHeartRate,
            Integer sedentaryMinutes,
            Integer steps,
            Integer veryActiveMinutes
    ) {
    }

    /**
     * Record representing a distance entry.
     */
    public record Distance(
            String activity,
            Double distance
    ) {
    }
}