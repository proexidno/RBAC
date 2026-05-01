package com.example.taxi.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(@NotBlank String subject, @NotBlank String role) {
}

