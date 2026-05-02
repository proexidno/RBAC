package com.example.taxi.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyStatsResponse(LocalDate date, long tripsCount, BigDecimal averagePrice) {
}

