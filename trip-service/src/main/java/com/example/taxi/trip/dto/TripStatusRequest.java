package com.example.taxi.trip.dto;

import com.example.taxi.trip.model.TripStatus;
import jakarta.validation.constraints.NotNull;

public record TripStatusRequest(@NotNull TripStatus status) {
}

