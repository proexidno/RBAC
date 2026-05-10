package com.example.taxi.notification.dto;

import com.example.taxi.notification.model.RecipientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationCreateRequest(
        @NotNull Long tripId,
        @NotNull RecipientType recipientType,
        @NotNull Long recipientId,
        @NotBlank String message) {
}

