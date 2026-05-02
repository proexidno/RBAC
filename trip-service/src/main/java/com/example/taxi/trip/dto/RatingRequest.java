package com.example.taxi.trip.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RatingRequest(@Min(1) @Max(5) int rating) {
}

