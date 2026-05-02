package com.example.taxi.trip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taxi.trip.client.NotificationClient;
import com.example.taxi.trip.client.UserClient;
import com.example.taxi.trip.dto.DriverDto;
import com.example.taxi.trip.dto.NotificationCreateRequest;
import com.example.taxi.trip.dto.PassengerDto;
import com.example.taxi.trip.dto.TripCreateRequest;
import com.example.taxi.trip.model.Trip;
import com.example.taxi.trip.model.TripStatus;
import com.example.taxi.trip.repository.TripRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {
    @Mock
    private TripRepository tripRepository;
    @Mock
    private UserClient userClient;
    @Mock
    private NotificationClient notificationClient;

    private TripService tripService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        tripService = new TripService(tripRepository, userClient, notificationClient, BigDecimal.valueOf(35));
    }

    @Test
    void createReservesDriverCalculatesPriceAndCreatesNotifications() {
        when(userClient.getPassenger(1L)).thenReturn(new PassengerDto(1L, "Alex", "alex@example.com", "+70000000000"));
        when(userClient.reserveDriver()).thenReturn(new DriverDto(2L, "Ivan", "ivan@example.com", "+71111111111", "A123BC", "BUSY"));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip trip = invocation.getArgument(0);
            ReflectionTestUtils.setField(trip, "id", 10L);
            return trip;
        });

        Trip trip = tripService.create(new TripCreateRequest(
                1L, "Campus", "Airport", new BigDecimal("12.40")));

        assertThat(trip.getPassengerId()).isEqualTo(1L);
        assertThat(trip.getDriverId()).isEqualTo(2L);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.ASSIGNED);
        assertThat(trip.getPrice()).isEqualByComparingTo("434.00");

        ArgumentCaptor<NotificationCreateRequest> notifications = ArgumentCaptor.forClass(NotificationCreateRequest.class);
        verify(notificationClient, org.mockito.Mockito.times(2)).create(notifications.capture());
        assertThat(notifications.getAllValues())
                .extracting(NotificationCreateRequest::recipientType)
                .containsExactly("PASSENGER", "DRIVER");
    }

    @Test
    void createReleasesReservedDriverWhenSavingTripFails() {
        when(userClient.getPassenger(1L)).thenReturn(new PassengerDto(1L, "Alex", "alex@example.com", "+70000000000"));
        when(userClient.reserveDriver()).thenReturn(new DriverDto(2L, "Ivan", "ivan@example.com", "+71111111111", "A123BC", "BUSY"));
        when(tripRepository.save(any(Trip.class))).thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> tripService.create(new TripCreateRequest(
                1L, "Campus", "Airport", new BigDecimal("12.40"))))
                .isInstanceOf(IllegalStateException.class);

        verify(userClient).updateDriverStatus(2L, "AVAILABLE");
        verify(notificationClient, never()).create(any());
    }

    @Test
    void updateStatusCompletesTripAndReleasesDriver() {
        Trip trip = trip(5L, 1L, 2L, TripStatus.STARTED);
        when(tripRepository.findById(5L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(trip)).thenReturn(trip);

        Trip updated = tripService.updateStatus(5L, TripStatus.COMPLETED);

        assertThat(updated.getStatus()).isEqualTo(TripStatus.COMPLETED);
        verify(userClient).updateDriverStatus(2L, "AVAILABLE");
        verify(notificationClient, org.mockito.Mockito.times(2)).create(any());
    }

    @Test
    void rateRejectsTripThatIsNotCompleted() {
        when(tripRepository.findById(5L)).thenReturn(Optional.of(trip(5L, 1L, 2L, TripStatus.STARTED)));

        assertThatThrownBy(() -> tripService.rate(5L, 5))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only completed trips can be rated");
    }

    @Test
    void dailyStatsRoundsAveragePrice() {
        TripRepository.DailyStatsProjection projection = new TripRepository.DailyStatsProjection() {
            @Override
            public Long getTripsCount() {
                return 3L;
            }

            @Override
            public BigDecimal getAveragePrice() {
                return new BigDecimal("123.456");
            }
        };
        LocalDate date = LocalDate.of(2026, 4, 28);
        when(tripRepository.dailyStats(
                OffsetDateTime.parse("2026-04-28T00:00:00Z"),
                OffsetDateTime.parse("2026-04-29T00:00:00Z")))
                .thenReturn(projection);

        var stats = tripService.dailyStats(date);

        assertThat(stats.tripsCount()).isEqualTo(3);
        assertThat(stats.averagePrice()).isEqualByComparingTo("123.46");
    }

    private Trip trip(Long id, Long passengerId, Long driverId, TripStatus status) {
        Trip trip = new Trip();
        ReflectionTestUtils.setField(trip, "id", id);
        trip.setPassengerId(passengerId);
        trip.setDriverId(driverId);
        trip.setStatus(status);
        trip.setOrigin("Campus");
        trip.setDestination("Airport");
        trip.setDistanceKm(BigDecimal.TEN);
        trip.setPrice(BigDecimal.valueOf(350));
        return trip;
    }
}
