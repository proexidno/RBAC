package com.example.taxi.user.dto;

import com.example.taxi.user.model.DriverStatus;
import jakarta.validation.constraints.NotNull;

public record DriverStatusRequest(@NotNull DriverStatus status) {
}

