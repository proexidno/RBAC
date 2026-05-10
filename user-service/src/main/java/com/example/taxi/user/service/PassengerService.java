package com.example.taxi.user.service;

import com.example.taxi.user.dto.PassengerCreateRequest;
import com.example.taxi.user.model.Passenger;
import com.example.taxi.user.repository.PassengerRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
        var existing = passengerRepository.findByEmail(request.email());
        if (existing.isPresent()) {
            return existing.get();
        }

        Passenger passenger = new Passenger();
        passenger.setName(request.name());
        passenger.setEmail(request.email());
        passenger.setPhone(request.phone());
        try {
            return passengerRepository.save(passenger);
        } catch (DataIntegrityViolationException ex) {
            return passengerRepository.findByEmail(request.email())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Passenger already exists", ex));
        }
    }

    public Passenger get(Long id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found"));
    }
}
