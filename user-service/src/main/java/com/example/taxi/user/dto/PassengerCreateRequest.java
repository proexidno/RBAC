package com.example.taxi.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PassengerCreateRequest(@NotBlank String name, @Email @NotBlank String email, @NotBlank String phone) {
}

