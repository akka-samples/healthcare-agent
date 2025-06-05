package io.akka.health.fitbit.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record class representing sleep log data from Fitbit API.
 */
public record SleepLogData(
    @JsonProperty("sleep") List<Sleep> sleep,
    @JsonProperty("summary") SleepSummary summary
) {
    /**
     * Record representing a sleep entry.
     */
    public record Sleep(
        @JsonProperty("dateOfSleep") LocalDate dateOfSleep,
        @JsonProperty("duration") Long duration,
        @JsonProperty("efficiency") Integer efficiency,
        @JsonProperty("endTime") LocalDateTime endTime,
        @JsonProperty("infoCode") Integer infoCode,
        @JsonProperty("isMainSleep") Boolean isMainSleep,
        @JsonProperty("levels") SleepLevels levels,
        @JsonProperty("logId") Long logId,
        @JsonProperty("minutesAfterWakeup") Integer minutesAfterWakeup,
        @JsonProperty("minutesAsleep") Integer minutesAsleep,
        @JsonProperty("minutesAwake") Integer minutesAwake,
        @JsonProperty("minutesToFallAsleep") Integer minutesToFallAsleep,
        @JsonProperty("startTime") LocalDateTime startTime,
        @JsonProperty("timeInBed") Integer timeInBed,
        @JsonProperty("type") String type
    ) {}

    /**
     * Record representing sleep levels.
     */
    public record SleepLevels(
        @JsonProperty("data") List<SleepLevelData> data,
        @JsonProperty("shortData") List<SleepLevelData> shortData,
        @JsonProperty("summary") SleepLevelSummary summary
    ) {}

    /**
     * Record representing sleep level data.
     */
    public record SleepLevelData(
        @JsonProperty("dateTime") LocalDateTime dateTime,
        @JsonProperty("level") String level,
        @JsonProperty("seconds") Integer seconds
    ) {}

    /**
     * Record representing sleep level summary.
     */
    public record SleepLevelSummary(
        @JsonProperty("deep") SleepLevelSummaryItem deep,
        @JsonProperty("light") SleepLevelSummaryItem light,
        @JsonProperty("rem") SleepLevelSummaryItem rem,
        @JsonProperty("wake") SleepLevelSummaryItem wake
    ) {}

    /**
     * Record representing a sleep level summary item.
     */
    public record SleepLevelSummaryItem(
        @JsonProperty("count") Integer count,
        @JsonProperty("minutes") Integer minutes,
        @JsonProperty("thirtyDayAvgMinutes") Integer thirtyDayAvgMinutes
    ) {}

    /**
     * Record representing sleep summary.
     */
    public record SleepSummary(
        @JsonProperty("stages") SleepStages stages,
        @JsonProperty("totalMinutesAsleep") Integer totalMinutesAsleep,
        @JsonProperty("totalSleepRecords") Integer totalSleepRecords,
        @JsonProperty("totalTimeInBed") Integer totalTimeInBed
    ) {}

    /**
     * Record representing sleep stages.
     */
    public record SleepStages(
        @JsonProperty("deep") Integer deep,
        @JsonProperty("light") Integer light,
        @JsonProperty("rem") Integer rem,
        @JsonProperty("wake") Integer wake
    ) {}
}