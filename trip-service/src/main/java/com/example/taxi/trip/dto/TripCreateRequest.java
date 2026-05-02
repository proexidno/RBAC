package com.example.taxi.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TripCreateRequest(
        @NotNull Long passengerId,
        @NotBlank String origin,
        @NotBlank String destination,
        @Positive BigDecimal distanceKm) {
}

