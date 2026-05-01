package com.example.taxi.user.service;

import com.example.taxi.user.dto.PassengerCreateRequest;
import com.example.taxi.user.model.Passenger;
import com.example.taxi.user.repository.PassengerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PassengerService {
    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public Passenger create(PassengerCreateRequest request) {
        Passenger passenger = new Passenger();
        passenger.setName(request.name());
        passenger.setEmail(request.email());
        passenger.setPhone(request.phone());
        return passengerRepository.save(passenger);
    }

    public Passenger get(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found"));
    }
}

