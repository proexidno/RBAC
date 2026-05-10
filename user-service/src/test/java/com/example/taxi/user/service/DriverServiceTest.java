package com.example.taxi.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taxi.user.dto.DriverCreateRequest;
import com.example.taxi.user.model.Driver;
import com.example.taxi.user.model.DriverStatus;
import com.example.taxi.user.repository.DriverRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ListOperations<String, String> listOperations;
    @Mock
    private EntityManager entityManager;
    @Mock
    private Query query;

    @InjectMocks
    private DriverService driverService;

    @Test
    void createSetsAvailableStatusAndEvictsCache() {
        when(driverRepository.findByLicenseNumber("A123BC")).thenReturn(Optional.empty());
        when(driverRepository.findByEmail("ivan@example.com")).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Driver driver = driverService.create(new DriverCreateRequest(
                "Ivan", "ivan@example.com", "+71111111111", "A123BC"));

        assertThat(driver.getStatus()).isEqualTo(DriverStatus.AVAILABLE);
        verify(redisTemplate).delete("drivers:available");
    }

    @Test
    void availableDriversLoadsFromRepositoryAndCachesIds() {
        Driver driver = driverWithId(7L, DriverStatus.AVAILABLE);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("drivers:available", 0, -1)).thenReturn(List.of());
        when(driverRepository.findByStatusOrderById(DriverStatus.AVAILABLE)).thenReturn(List.of(driver));

        List<Driver> drivers = driverService.availableDrivers();

        assertThat(drivers).containsExactly(driver);
        verify(listOperations).rightPushAll("drivers:available", List.of("7"));
        verify(redisTemplate).expire("drivers:available", 30, TimeUnit.SECONDS);
    }

    @Test
    void availableDriversFiltersStaleCachedBusyDrivers() {
        Driver available = driverWithId(1L, DriverStatus.AVAILABLE);
        Driver busy = driverWithId(2L, DriverStatus.BUSY);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("drivers:available", 0, -1)).thenReturn(List.of("1", "2"));
        when(driverRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(available, busy));

        assertThat(driverService.availableDrivers()).containsExactly(available);
    }

    @Test
    void reserveAvailableDriverReturnsClaimedDriverAndEvictsCache() {
        Driver driver = driverWithId(9L, DriverStatus.BUSY);
        when(entityManager.createNativeQuery(org.mockito.ArgumentMatchers.contains("FOR UPDATE SKIP LOCKED"), eq(Driver.class)))
                .thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(driver));

        Optional<Driver> reserved = driverService.reserveAvailableDriver();

        assertThat(reserved).contains(driver);
        verify(redisTemplate).delete("drivers:available");
    }

    private Driver driverWithId(Long id, DriverStatus status) {
        Driver driver = new Driver();
        ReflectionTestUtils.setField(driver, "id", id);
        driver.setStatus(status);
        return driver;
    }
}
