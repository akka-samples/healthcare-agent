package io.akka.health.fitbit;

import akka.http.javadsl.model.headers.HttpCredentials;
import akka.javasdk.http.HttpClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.akka.health.fitbit.model.*;
import io.akka.health.common.KeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class FitbitClient {
    private static final String AUTH_URL = "/oauth2/authorize"; // The base url is already passed into the FitbitClient in the Boostrap class
    private static final String TOKEN_URL = "/oauth2/token"; // The base url is already passed into the FitbitClient in the Boostrap class
    private static final String API_BASE_URL = ""; // The base url is already passed into the FitbitClient in the Boostrap class
    private static final String REDIRECT_URI = "https://janikdotzel.com/";
    private static final String SCOPE = "heartrate activity sleep weight";
    private static final Logger logger = LoggerFactory.getLogger(FitbitClient.class);

    private final ObjectMapper objectMapper;
    private final FitbitParser parser;
    private final String clientId;
    private final String clientSecret;
    private final HttpClient httpClient;

    private String accessToken;
    private String refreshToken;
    private long expiresAt;
    private String codeVerifier;

    public FitbitClient(HttpClient httpClient) {
        this.objectMapper = new ObjectMapper();
        this.parser = new FitbitParser();
        this.clientId = KeyUtils.readFitbitClientId();
        this.clientSecret = KeyUtils.readFitbitClientSecret();
        this.httpClient = httpClient;

        if (KeyUtils.hasFitbitAccessToken()) {
            setTokens(KeyUtils.readFitbitAccessToken(), "", 28800);
            logger.info("Using access token from environment variable");
        } else if (KeyUtils.hasFitbitKeys()) {
            logger.info("Using client ID and secret from environment variable");
        } else {
            throw new IllegalStateException("Fitbit API keys and Access Token not found. Make sure FITBIT_CLIENT_ID and FITBIT_CLIENT_SECRET or FITBIT_ACCESS_TOKEN are defined as environment variables or in the .env file.");
        }
    }

    public String getAuthorizationUrl() {
        if (this.codeVerifier == null) {
            generateCodeVerifier();
        }

        String codeChallenge = generateCodeChallenge(this.codeVerifier);

        return AUTH_URL + "?" +
                "client_id=" + clientId +
                "&response_type=code" +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256" +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8);
    }

    public TokenResponse exchangeCodeForAccessToken(String authCode) {
        if (this.codeVerifier == null) {
            throw new IllegalStateException("Code verifier not generated. Call getAuthorizationUrl() first.");
        }

        String authHeader = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        logger.info("Using client ID: {}", clientId);
        logger.debug("Authorization header: {}", authHeader);

        // Create form data string
        String formDataString = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&code=" + URLEncoder.encode(authCode, StandardCharsets.UTF_8) +
                "&code_verifier=" + URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8) +
                "&grant_type=authorization_code" +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

        logger.debug("Request body: {}", formDataString);
        logger.info("Sending request to Fitbit API token endpoint: {}", TOKEN_URL);

        var response = httpClient
                .POST(TOKEN_URL)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", authHeader)
                .withRequestBody(formDataString)
                .invoke();

        int statusCode = response.status().intValue();
        String responseBody = response.body().utf8String();

        logger.info("Received response with status code: {}", statusCode);
        logger.debug("Response body: {}", responseBody);

        if (statusCode == 200) {
            try {
                logger.info("Successfully obtained access token");
                return parseTokenResponse(responseBody);
            } catch (Exception e) {
                logger.error("Failed to parse token response", e);
                throw new RuntimeException("Failed to parse token response", e);
            }
        } else {
            logger.error("Failed to get token with authorization code: {} - {}", statusCode, responseBody);

            // Check for specific error conditions
            if (statusCode == 403) {
                logger.error("403 Forbidden error. This could be due to incorrect client ID/secret, " +
                        "invalid scope, or the application not being registered as an OAuth 2.0 Server type.");
            }

            throw new RuntimeException("Failed to get token with authorization code: " + statusCode + " - " + responseBody);
        }
    }

    public HeartRateData getHeartRateByDate(LocalDate date) {
        ensureValidToken();

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
        ensureValidToken();

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
        ensureValidToken();

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
        ensureValidToken();

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
        ensureValidToken();

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

    private void ensureValidToken() {
        if (accessToken == null)
            throw new IllegalStateException("No access token available");

        if (System.currentTimeMillis() >= expiresAt)
            refreshAccessToken();
    }

    private TokenResponse parseTokenResponse(String json) {
        try {
            TokenResponse response = objectMapper.readValue(json, TokenResponse.class);
            setTokens(response.accessToken, response.refreshToken, response.expiresIn);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token response", e);
        }
    }

    public void setTokens(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = System.currentTimeMillis() + (expiresIn * 1000);
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

    private TokenResponse refreshAccessToken() {
        if (refreshToken == null) {
            throw new IllegalStateException("No refresh token available");
        }

        String authHeader = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "refresh_token");
        formData.put("refresh_token", refreshToken);

        String formDataString = formData.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        var response = httpClient
                .POST(TOKEN_URL)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", authHeader)
                .withRequestBody(formDataString)
                .invoke();

        if (response.status().intValue() == 200) {
            try {
                return parseTokenResponse(response.body().utf8String());
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse token response", e);
            }
        } else {
            throw new RuntimeException("Failed to refresh token: " + response.status() + " - " + response.body().utf8String());
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
