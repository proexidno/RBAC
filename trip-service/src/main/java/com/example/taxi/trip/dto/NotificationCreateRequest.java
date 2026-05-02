package com.example.taxi.trip.dto;

public record NotificationCreateRequest(Long tripId, String recipientType, Long recipientId, String message) {
}

