package com.example.taxi.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taxi.user.dto.PassengerCreateRequest;
import com.example.taxi.user.model.Passenger;
import com.example.taxi.user.repository.PassengerRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {
    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private PassengerService passengerService;

    @Test
    void createMapsRequestToEntity() {
        when(passengerRepository.findByEmail("alex@example.com")).thenReturn(Optional.empty());
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Passenger created = passengerService.create(new PassengerCreateRequest(
                "Alex", "alex@example.com", "+70000000000"));

        assertThat(created.getName()).isEqualTo("Alex");
        assertThat(created.getEmail()).isEqualTo("alex@example.com");
        assertThat(created.getPhone()).isEqualTo("+70000000000");
        verify(passengerRepository).save(any(Passenger.class));
    }

    @Test
    void getThrowsNotFoundWhenPassengerDoesNotExist() {
        when(passengerRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passengerService.get(404L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Passenger not found");
    }

    @Test
    void getReturnsExistingPassenger() {
        Passenger passenger = new Passenger();
        passenger.setName("Alex");
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));

        assertThat(passengerService.get(1L)).isSameAs(passenger);
    }
}
