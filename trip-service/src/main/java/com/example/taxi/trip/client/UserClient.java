package com.example.taxi.trip.client;

import com.example.taxi.trip.auth.JwtUtil;
import com.example.taxi.trip.dto.DriverDto;
import com.example.taxi.trip.dto.DriverStatusUpdateRequest;
import com.example.taxi.trip.dto.PassengerDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserClient {
    private final RestClient restClient;
    private final JwtUtil jwtUtil;

    public UserClient(RestClient.Builder builder,
                      @Value("${services.user-service-url}") String baseUrl,
                      JwtUtil jwtUtil) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.jwtUtil = jwtUtil;
    }

    public PassengerDto getPassenger(Long passengerId) {
        return restClient.get()
                .uri("/passengers/{id}", passengerId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(PassengerDto.class);
    }

    public DriverDto reserveDriver() {
        return restClient.post()
                .uri("/drivers/reserve")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(DriverDto.class);
    }

    public void updateDriverStatus(Long driverId, String status) {
        restClient.patch()
                .uri("/drivers/{id}/status", driverId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .body(new DriverStatusUpdateRequest(status))
                .retrieve()
                .toBodilessEntity();
    }

    private String bearer() {
        return "Bearer " + jwtUtil.serviceToken();
    }
}

