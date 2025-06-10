package io.akka.health.fitbit;

import akka.http.javadsl.model.headers.HttpCredentials;
import akka.javasdk.http.HttpClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.akka.health.fitbit.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;


public class FitbitClient {
    private static final String API_BASE_URL = ""; // The base url is already passed into the FitbitClient in the Boostrap class
    private static final Logger logger = LoggerFactory.getLogger(FitbitClient.class);

    private final ObjectMapper objectMapper;
    private final FitbitParser parser;
    private final HttpClient httpClient;

    private String accessToken;
    private String codeVerifier;

    public FitbitClient(HttpClient httpClient) {
        this.objectMapper = new ObjectMapper();
        this.parser = new FitbitParser();
        this.httpClient = httpClient;
    }

    public HeartRateData getHeartRateByDate(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = API_BASE_URL + "/1/user/-/activities/heart/date/" + dateStr + "/1d.json";

        var response = httpClient
                .GET(url)
                .addCredentials(HttpCredentials.createOAuth2BearerToken(accessToken))
                .invoke();

        if (response.status().intValue() == 200) {
            try {
                return parser.parseHeartRateData(response.body().utf8String());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse heart rate data", e);
            }
        } else {
            throw new RuntimeException("Failed to get heart rate data: " + response.status() + " - " + response.body().utf8String());
        }
    }

    public ActiveZoneMinutesData getActiveZoneMinutesByDate(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = API_BASE_URL + "/1/user/-/activities/active-zone-minutes/date/" + dateStr + "/1d.json";

        var response = httpClient
                .GET(url)
                .addCredentials(HttpCredentials.createOAuth2BearerToken(accessToken))
                .invoke();

        if (response.status().intValue() == 200) {
            try {
                return parser.parseActiveZoneMinutesData(response.body().utf8String());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse Active Zone Minutes data", e);
            }
        } else {
            throw new RuntimeException("Failed to get Active Zone Minutes data: " + response.status() + " - " + response.body().utf8String());
        }
    }

    public SleepLogData getSleepLogByDate(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = API_BASE_URL + "/1.2/user/-/sleep/date/" + dateStr + ".json";

        var response = httpClient
                .GET(url)
                .addCredentials(HttpCredentials.createOAuth2BearerToken(accessToken))
                .invoke();

        if (response.status().intValue() == 200) {
            try {
                return parser.parseSleepLogData(response.body().utf8String());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse sleep log data", e);
            }
        } else {
            throw new RuntimeException("Failed to get sleep log data: " + response.status() + " - " + response.body().utf8String());
        }
    }

    public WeightLogData getWeightLogByDate(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = API_BASE_URL + "/1/user/-/body/log/weight/date/" + dateStr + ".json";

        var response = httpClient
                .GET(url)
                .addCredentials(HttpCredentials.createOAuth2BearerToken(accessToken))
                .invoke();

        if (response.status().intValue() == 200) {
            try {
                return parser.parseWeightLogData(response.body().utf8String());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse weight log data", e);
            }
        } else {
            throw new RuntimeException("Failed to get weight log data: " + response.status() + " - " + response.body().utf8String());
        }
    }

    public DailyActivitySummary getDailyActivitySummary(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = API_BASE_URL + "/1/user/-/activities/date/" + dateStr + ".json";

        var response = httpClient
                .GET(url)
                .addCredentials(HttpCredentials.createOAuth2BearerToken(accessToken))
                .invoke();

        logger.info("Response: {}", response.body().utf8String());
        logger.info("Status: {}", response.status().intValue());


        if (response.status().intValue() == 200) {
            try {
                return parser.parseDailyActivitySummary(response.body().utf8String());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse daily activity summary", e);
            }
        } else {
            throw new RuntimeException("Failed to get daily activity summary: " + response.status() + " - " + response.body().utf8String());
        }
    }

    private TokenResponse parseTokenResponse(String json) {
        try {
            TokenResponse response = objectMapper.readValue(json, TokenResponse.class);
            this.accessToken = response.accessToken;
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token response", e);
        }
    }

    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifierBytes = new byte[64]; // 64 bytes will give us a 86-character code verifier
        secureRandom.nextBytes(codeVerifierBytes);
        this.codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
        return this.codeVerifier;
    }

    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate code challenge: SHA-256 algorithm not available", e);
        }
    }

    public static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("expires_in")
        private long expiresIn;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("scope")
        private String scope;

        @JsonProperty("user_id")
        private String userId;

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public String getTokenType() {
            return tokenType;
        }

        public String getScope() {
            return scope;
        }

        public String getUserId() {
            return userId;
        }
    }
}
