package io.akka.health.agent.application;


import akka.javasdk.agent.Agent;
import akka.javasdk.agent.MemoryProvider;
import akka.javasdk.annotations.AgentDescription;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.FunctionTool;
import akka.javasdk.client.ComponentClient;
import io.akka.health.fitbit.FitbitClient;

import com.mongodb.client.MongoClient;
import io.akka.health.fitbit.model.DailyActivitySummary;
import io.akka.health.agent.model.HealthAgentRequest;
import io.akka.health.ingest.application.SensorView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@ComponentId("health-agent")
@AgentDescription(name = "Health Agent", description = "A personal health assistant with knowledge about the user's health data.")
public class HealthAgent extends Agent {

  private final static Logger logger = LoggerFactory.getLogger(HealthAgent.class);
  private final ComponentClient componentClient;
  private final String systemMessage = """
    You are a personal health assistant that helps the user to stay healthy.
    You have access to the user's health data that is observed through fitness trackers and made available through Fitbit.
    You have access to the user's medical records.
    Answer the question in a concise way.
    """;
  private final FitbitTool fitbitTool;
  private final SensorTool sensorTool;
  private final MedicalRecordRAG medicalRecordRAG;

  public HealthAgent(ComponentClient componentClient, MongoClient mongoClient, FitbitClient fitbitClient) {
    this.componentClient = componentClient;
    this.fitbitTool = new FitbitTool(fitbitClient);
    this.sensorTool = new SensorTool(componentClient);
    this.medicalRecordRAG = new MedicalRecordRAG(mongoClient);
  }

  public Agent.Effect<String> ask(HealthAgentRequest request) {
    String promptTemplate = """
        Question: %s
        Knowledge: %s
        UserId: %s
        """;

    String knowledge = medicalRecordRAG.retrieve(request.question(), request.userId());
    String prompt = promptTemplate.formatted(request.question(), knowledge, request.userId());

    logger.info("Processing request: {}", prompt);

    return effects()
            .memory(MemoryProvider.limitedWindow())
            .systemMessage(systemMessage)
            .userMessage(prompt)
            .thenReply();
  }

  @FunctionTool(description = "Get all Sensor Data for a specific user")
  private SensorView.AllSensorData getSensorData(String userId) {
    return sensorTool.getSensorData(userId);
  }

  @FunctionTool(description = "Get resting heart rate for a specific date")
  public Integer restingHeartRate(String date) {
    LocalDate parsedDate = LocalDate.parse(date);
    return fitbitTool.restingHeartRate(parsedDate);
  }

  @FunctionTool(description = "Check if heart rate (in bpm) exceeded the range for a specific date. If exceeded, it returns the value that exceeded the range the most. Otherwise it reurns 0.")
  public Integer isHeartRateOutsideSafeRange(String date, int minThreshold, int maxThreshold) {
    LocalDate parsedDate = LocalDate.parse(date);
    return fitbitTool.isHeartRateOutsideSafeRange(parsedDate, minThreshold, maxThreshold);
  }

  @FunctionTool(description = "Get total active minutes for a specific date range (usually one week).")
  public Integer getActiveMinutesInWeek(String startDate, String endDate) {
    LocalDate start = LocalDate.parse(startDate);
    LocalDate end = LocalDate.parse(endDate);
    return fitbitTool.getActiveMinutesInWeek(start, end);
  }

  @FunctionTool(description = "Get amount of sleep hours for a specific date.")
  public Double getSleepHoursForDay(String date) {
    LocalDate parsedDate = LocalDate.parse(date);
    return fitbitTool.getSleepHoursForDay(parsedDate);
  }

  @FunctionTool(description = "Get amount of REM sleep in minutes for a specific date.")
  public Integer getRemSleepMinutes(String date) {
    LocalDate parsedDate = LocalDate.parse(date);
    return fitbitTool.getRemSleepMinutes(parsedDate);
  }

  @FunctionTool(description = "Get all sport activities (sport, gym, aerobic) for a specific date range (usually one week).")
  public List<DailyActivitySummary.Activity> getSportActivitiesInWeek(String startDate, String endDate) {
    LocalDate start = LocalDate.parse(startDate);
    LocalDate end = LocalDate.parse(endDate);
    return fitbitTool.getSportActivitiesInWeek(start, end);
  }

  @FunctionTool(description = "Get number of steps walked for a specific date.")
  public Integer getStepsForDay(String date) {
    LocalDate parsedDate = LocalDate.parse(date);
    return fitbitTool.getStepsForDay(parsedDate);
  }
}
