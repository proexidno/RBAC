package com.example.taxi.trip.service;

import com.example.taxi.trip.client.NotificationClient;
import com.example.taxi.trip.client.UserClient;
import com.example.taxi.trip.dto.DailyStatsResponse;
import com.example.taxi.trip.dto.DriverDto;
import com.example.taxi.trip.dto.NotificationCreateRequest;
import com.example.taxi.trip.dto.TripCreateRequest;
import com.example.taxi.trip.model.Trip;
import com.example.taxi.trip.model.TripStatus;
import com.example.taxi.trip.repository.TripRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TripService {
    private final TripRepository tripRepository;
    private final UserClient userClient;
    private final NotificationClient notificationClient;
    private final BigDecimal tariffPerKm;

    public TripService(TripRepository tripRepository,
                       UserClient userClient,
                       NotificationClient notificationClient,
                       @Value("${taxi.tariff-per-km}") BigDecimal tariffPerKm) {
        this.tripRepository = tripRepository;
        this.userClient = userClient;
        this.notificationClient = notificationClient;
        this.tariffPerKm = tariffPerKm;
    }

    public Trip create(TripCreateRequest request) {
        DriverDto driver;
        try {
            userClient.getPassenger(request.passengerId());
            driver = userClient.reserveDriver();
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        }

        BigDecimal distance = request.distanceKm() == null
                ? estimateDistance(request.origin(), request.destination())
                : request.distanceKm();

        Trip trip = new Trip();
        trip.setPassengerId(request.passengerId());
        trip.setDriverId(driver.id());
        trip.setStatus(TripStatus.ASSIGNED);
        trip.setOrigin(request.origin());
        trip.setDestination(request.destination());
        trip.setDistanceKm(distance.setScale(2, RoundingMode.HALF_UP));
        trip.setPrice(distance.multiply(tariffPerKm).setScale(2, RoundingMode.HALF_UP));

        Trip saved;
        try {
            saved = tripRepository.save(trip);
        } catch (RuntimeException ex) {
            userClient.updateDriverStatus(driver.id(), "AVAILABLE");
            throw ex;
        }
        notifyTripAssigned(saved);
        return saved;
    }

    public Trip get(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
    }

    public List<Trip> byPassenger(Long passengerId) {
        return tripRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);
    }

    public Trip updateStatus(Long id, TripStatus status) {
        Trip trip = get(id);
        trip.setStatus(status);
        Trip saved = tripRepository.save(trip);

        if (status == TripStatus.COMPLETED || status == TripStatus.CANCELLED) {
            userClient.updateDriverStatus(saved.getDriverId(), "AVAILABLE");
        } else if (status == TripStatus.ACCEPTED || status == TripStatus.STARTED) {
            userClient.updateDriverStatus(saved.getDriverId(), "BUSY");
        }

        notifyStatusChanged(saved);
        return saved;
    }

    public Trip rate(Long id, int rating) {
        Trip trip = get(id);
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only completed trips can be rated");
        }
        trip.setRating(rating);
        return tripRepository.save(trip);
    }

    public DailyStatsResponse dailyStats(LocalDate date) {
        OffsetDateTime start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = start.plusDays(1);
        var stats = tripRepository.dailyStats(start, end);
        long count = stats.getTripsCount();
        BigDecimal average = stats.getAveragePrice();
        return new DailyStatsResponse(date, count, average.setScale(2, RoundingMode.HALF_UP));
    }

    private void notifyTripAssigned(Trip trip) {
        notificationClient.create(new NotificationCreateRequest(
                trip.getId(), "PASSENGER", trip.getPassengerId(),
                "Trip " + trip.getId() + " assigned to driver " + trip.getDriverId()));
        notificationClient.create(new NotificationCreateRequest(
                trip.getId(), "DRIVER", trip.getDriverId(),
                "You have been assigned to trip " + trip.getId()));
    }

    private void notifyStatusChanged(Trip trip) {
        String message = "Trip " + trip.getId() + " status changed to " + trip.getStatus();
        notificationClient.create(new NotificationCreateRequest(trip.getId(), "PASSENGER", trip.getPassengerId(), message));
        notificationClient.create(new NotificationCreateRequest(trip.getId(), "DRIVER", trip.getDriverId(), message));
    }

    private BigDecimal estimateDistance(String origin, String destination) {
        int hash = Math.abs((origin + ":" + destination).hashCode());
        return BigDecimal.valueOf(3 + (hash % 2500) / 100.0);
    }
}
