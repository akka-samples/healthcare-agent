package io.akka.health.ingest.domain;

/**
 * Represents the data collected from a sensor.
 *
 * @param userId   The unique identifier for the patient (e.g., "12345").
 * @param source      The source of the sensor data (e.g., "smartwatch", "fitness-tracker", "medical-device").
 * @param description A brief description of the sensor (e.g., "temperature", "heart-rate", "blood-pressure").
 * @param value       The value recorded by the sensor (e.g., "98.6°F", "72 bpm", "120/80 mmHg").
 */
public record SensorData(String userId, String source, String description, String value) {}
