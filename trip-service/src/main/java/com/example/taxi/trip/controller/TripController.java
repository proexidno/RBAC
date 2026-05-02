package com.example.taxi.trip.controller;

import com.example.taxi.trip.dto.DailyStatsResponse;
import com.example.taxi.trip.dto.RatingRequest;
import com.example.taxi.trip.dto.TripCreateRequest;
import com.example.taxi.trip.dto.TripStatusRequest;
import com.example.taxi.trip.model.Trip;
import com.example.taxi.trip.service.TripService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TripController {
    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/trips")
    Trip create(@Valid @RequestBody TripCreateRequest request) {
        return tripService.create(request);
    }

    @GetMapping("/trips/{id}")
    Trip get(@PathVariable Long id) {
        return tripService.get(id);
    }

    @GetMapping(value = "/trips", params = "passenger_id")
    List<Trip> byPassenger(@RequestParam("passenger_id") Long passengerId) {
        return tripService.byPassenger(passengerId);
    }

    @PatchMapping("/trips/{id}/status")
    Trip updateStatus(@PathVariable Long id, @Valid @RequestBody TripStatusRequest request) {
        return tripService.updateStatus(id, request.status());
    }

    @PostMapping("/trips/{id}/rating")
    Trip rate(@PathVariable Long id, @Valid @RequestBody RatingRequest request) {
        return tripService.rate(id, request.rating());
    }

    @GetMapping("/trips/statistics/daily")
    DailyStatsResponse dailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tripService.dailyStats(date);
    }
}

