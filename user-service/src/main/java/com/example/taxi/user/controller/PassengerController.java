package com.example.taxi.user.controller;

import com.example.taxi.user.dto.PassengerCreateRequest;
import com.example.taxi.user.model.Passenger;
import com.example.taxi.user.service.PassengerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PassengerController {
    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @PostMapping("/passengers")
    Passenger create(@Valid @RequestBody PassengerCreateRequest request) {
        return passengerService.create(request);
    }

    @GetMapping("/passengers/{id}")
    Passenger get(@PathVariable Long id) {
        return passengerService.get(id);
    }
}

