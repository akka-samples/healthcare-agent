package io.akka.health.agent.application;

import dev.langchain4j.agent.tool.Tool;
import io.akka.health.fitbit.FitbitClient;
import io.akka.health.fitbit.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FitbitTool {

    private final FitbitClient fitbitClient;
    private final static Logger logger = LoggerFactory.getLogger(FitbitTool.class);

    public FitbitTool(FitbitClient fitbitClient) {
        this.fitbitClient = fitbitClient;
    }

    @Tool("Get resting heart rate for a specific date")
    public Integer restingHeartRate(LocalDate date) {
        logger.info("Getting resting heart rate for date {}", date);

        var data = fitbitClient.getHeartRateByDate(date);

        if (data.activitiesHeart().isEmpty() || data.activitiesHeart().getFirst().value().restingHeartRate() == null) {
            return -1;
        }

        HeartRateData.HeartRateValue value = data.activitiesHeart().getFirst().value();
        return value.restingHeartRate();
    }

    @Tool("Check if heart rate (in bpm) exceeded the range for a specific date. If exceeded, it returns the value that exceeded the range the most. Otherwise it reurns 0.")
    public Integer isHeartRateOutsideSafeRange(LocalDate date, int minThreshold, int maxThreshold) {
        logger.info("Checking heart rate for date {} with thresholds {} - {}", date, minThreshold, maxThreshold);

        var data = fitbitClient.getHeartRateByDate(date);

        if (data.activitiesHeartIntraday() == null || data.activitiesHeartIntraday().dataset() == null || data.activitiesHeartIntraday().dataset().isEmpty()) {
            return 0;
        }

        List<HeartRateData.HeartRateDataPoint> dataPoints = data.activitiesHeartIntraday().dataset();

        Integer maxDeviation = null;
        Integer mostExtremeValue = null;

        for (HeartRateData.HeartRateDataPoint point : dataPoints) {
            int value = point.value();
            int deviation = 0;

            if (value < minThreshold) {
                deviation = minThreshold - value;
            } else if (value > maxThreshold) {
                deviation = value - maxThreshold;
            }

            if (deviation > 0 && (maxDeviation == null || deviation > maxDeviation)) {
                maxDeviation = deviation;
                mostExtremeValue = value;
            }
        }

        return mostExtremeValue != null ? mostExtremeValue : 0;
    }

    @Tool("Get total active minutes for a specific date range (usually one week).")
    public Integer getActiveMinutesInWeek(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting active minutes from {} to {}", startDate, endDate);
        List<ActiveZoneMinutesData> list = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            list.add(fitbitClient.getActiveZoneMinutesByDate(currentDate));
            currentDate = currentDate.plusDays(1);
        }

        int totalActiveMinutes = 0;

        for (ActiveZoneMinutesData azm : list) {
            if (azm.activitiesActiveZoneMinutes() != null && !azm.activitiesActiveZoneMinutes().isEmpty()) {
                ActiveZoneMinutesData.ActiveZoneMinutesValue value = azm.activitiesActiveZoneMinutes().getFirst().value();

                if (value.activeZoneMinutes() != null) {
                    totalActiveMinutes += value.activeZoneMinutes();
                }
            }
        }

        return totalActiveMinutes;
    }

    @Tool("Get amount of sleep hours for a specific date.")
    public Double getSleepHoursForDay(LocalDate date) {
        logger.info("Getting sleep hours for date {}", date);

        var data = fitbitClient.getSleepLogByDate(date);

        if (data.summary() != null && data.summary().totalMinutesAsleep() != null) {
            return data.summary().totalMinutesAsleep() / 60.0;
        } else {
            return 0.0;
        }
    }

    @Tool("Get amount of REM sleep in minutes for a specific date.")
    public Integer getRemSleepMinutes(LocalDate date) {
        logger.info("Getting REM sleep minutes for date {}", date);

        var data = fitbitClient.getSleepLogByDate(date);

        if (data.sleep() == null || data.sleep().isEmpty())
            return 0;

        int totalRemMinutes = 0;

        for (SleepLogData.Sleep sleep : data.sleep()) {
            if (sleep.levels() != null && sleep.levels().summary() != null && sleep.levels().summary().rem() != null && sleep.levels().summary().rem().minutes() != null) {
                totalRemMinutes += sleep.levels().summary().rem().minutes();
            }
        }

        return totalRemMinutes;
    }

    @Tool("Get all sport activities (sport, gym, aerobic) for a specific date range (usually one week).")
    public List<DailyActivitySummary.Activity> getSportActivitiesInWeek(LocalDate startDate, LocalDate endDate) {
        logger.info("Getting sport activities from {} to {}", startDate, endDate);
        List<DailyActivitySummary> list = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            list.add(fitbitClient.getDailyActivitySummary(currentDate));
            currentDate = currentDate.plusDays(1);
        }

        List<DailyActivitySummary.Activity> sportActivities = new ArrayList<>();

        for (DailyActivitySummary summary : list) {
            if (summary.activities() != null) {
                // Collect activities that are sports or intensive (like gym or aerobic)
                List<DailyActivitySummary.Activity> intensiveActivities = summary.activities().stream()
                        .filter(activity -> {
                            String name = activity.name() != null ? activity.name().toLowerCase() : "";
                            String parentName = activity.activityParentName() != null ? activity.activityParentName().toLowerCase() : "";

                            return name.contains("sport") || name.contains("gym") || name.contains("aerobic") ||
                                    parentName.contains("sport") || parentName.contains("gym") || parentName.contains("aerobic");
                        })
                        .toList();

                sportActivities.addAll(intensiveActivities);
            }
        }

        return sportActivities;
    }

    @Tool("Get number of steps walked for a specific date.")
    public Integer getStepsForDay(LocalDate date) {
        logger.info("Getting steps for date {}", date);

        var data = fitbitClient.getDailyActivitySummary(date);

        if (data.summary() != null && data.summary().steps() != null)
            return data.summary().steps();
        else
            return 0;
    }
}
