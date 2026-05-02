package com.example.taxi.trip.client;

import com.example.taxi.trip.auth.JwtUtil;
import com.example.taxi.trip.dto.NotificationCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationClient {
    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient restClient;
    private final JwtUtil jwtUtil;

    public NotificationClient(RestClient.Builder builder,
                              @Value("${services.notification-service-url}") String baseUrl,
                              JwtUtil jwtUtil) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.jwtUtil = jwtUtil;
    }

    public void create(NotificationCreateRequest request) {
        try {
            restClient.post()
                    .uri("/notifications")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.serviceToken())
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ex) {
            log.warn("Notification service is unavailable, trip flow continues: {}", ex.getMessage());
        }
    }
}

