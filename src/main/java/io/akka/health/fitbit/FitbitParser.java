package io.akka.health.fitbit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.akka.health.fitbit.model.*;

/**
 * Parser for Fitbit API responses.
 */
public class FitbitParser {
    private final ObjectMapper objectMapper;

    /**
     * Creates a new FitbitParser.
     */
    public FitbitParser() {
        this.objectMapper = new ObjectMapper();
        // Configure ObjectMapper for handling Java 8 date/time types
        objectMapper.registerModule(new JavaTimeModule());
        // Configure ObjectMapper to ignore unknown properties
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Parses heart rate data from JSON.
     *
     * @param json The JSON string to parse.
     * @return The parsed heart rate data.
     * @throws Exception If parsing fails.
     */
    public HeartRateData parseHeartRateData(String json) throws Exception {
        return objectMapper.readValue(json, HeartRateData.class);
    }

    /**
     * Parses active zone minutes data from JSON.
     *
     * @param json The JSON string to parse.
     * @return The parsed active zone minutes data.
     * @throws Exception If parsing fails.
     */
    public ActiveZoneMinutesData parseActiveZoneMinutesData(String json) throws Exception {
        return objectMapper.readValue(json, ActiveZoneMinutesData.class);
    }

    /**
     * Parses sleep log data from JSON.
     *
     * @param json The JSON string to parse.
     * @return The parsed sleep log data.
     * @throws Exception If parsing fails.
     */
    public SleepLogData parseSleepLogData(String json) throws Exception {
        return objectMapper.readValue(json, SleepLogData.class);
    }

    /**
     * Parses weight log data from JSON.
     *
     * @param json The JSON string to parse.
     * @return The parsed weight log data.
     * @throws Exception If parsing fails.
     */
    public WeightLogData parseWeightLogData(String json) throws Exception {
        return objectMapper.readValue(json, WeightLogData.class);
    }

    /**
     * Parses daily activity summary data from JSON.
     *
     * @param json The JSON string to parse.
     * @return The parsed daily activity summary data.
     * @throws Exception If parsing fails.
     */
    public DailyActivitySummary parseDailyActivitySummary(String json) throws Exception {
        return objectMapper.readValue(json, DailyActivitySummary.class);
    }
}