package com.codesupreme.boxingclub.service.impl.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OneSignalPushService {

    private static final String EXTERNAL_ID_PREFIX = "boxing_customer_";

    private static final URI PUSH_URI =
            URI.create("https://api.onesignal.com/notifications?c=push");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String appId;
    private final String restApiKey;

    public OneSignalPushService(
            ObjectMapper objectMapper,
            @Value("${onesignal.app-id:}") String appId,
            @Value("${onesignal.rest-api-key:}") String restApiKey
    ) {
        this.objectMapper = objectMapper;
        this.appId = appId == null ? "" : appId.trim();
        this.restApiKey = restApiKey == null ? "" : restApiKey.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isConfigured() {
        return !appId.isBlank() && !restApiKey.isBlank();
    }

    public boolean sendToCustomer(
            String customerId,
            String title,
            String message,
            String eventKey
    ) {
        if (!isConfigured() || customerId == null || customerId.isBlank()) {
            return false;
        }

        try {
            String externalId = EXTERNAL_ID_PREFIX + customerId.trim();

            String idempotencyKey = UUID.nameUUIDFromBytes(
                    eventKey.getBytes(StandardCharsets.UTF_8)
            ).toString();

            Map<String, Object> payload = Map.of(
                    "app_id", appId,
                    "headings", Map.of("en", title),
                    "contents", Map.of("en", message),
                    "include_aliases", Map.of(
                            "external_id",
                            List.of(externalId)
                    ),
                    "target_channel", "push",
                    "idempotency_key", idempotencyKey,
                    "custom_data", Map.of(
                            "type", "membership_expiry",
                            "customer_id", customerId,
                            "event_key", eventKey
                    )
            );

            HttpRequest request = HttpRequest.newBuilder(PUSH_URI)
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Key " + restApiKey)
                    .header("Content-Type", "application/json")
                    .POST(
                            HttpRequest.BodyPublishers.ofString(
                                    objectMapper.writeValueAsString(payload)
                            )
                    )
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println(
                        "OneSignal HTTP xətası: " + response.statusCode()
                                + " | " + response.body()
                );
                return false;
            }

            String notificationId = objectMapper
                    .readTree(response.body())
                    .path("id")
                    .asText("");

            if (notificationId.isBlank()) {
                System.err.println(
                        "OneSignal bildirişi yaradılmadı: " + response.body()
                );
                return false;
            }

            return true;
        } catch (Exception exception) {
            System.err.println(
                    "OneSignal push göndərilərkən xəta: " + exception.getMessage()
            );
            return false;
        }
    }
}
