package com.example.taxi.user.service;

import com.example.taxi.user.dto.DriverCreateRequest;
import com.example.taxi.user.model.Driver;
import com.example.taxi.user.model.DriverStatus;
import com.example.taxi.user.repository.DriverRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DriverService {
    private static final String AVAILABLE_CACHE_KEY = "drivers:available";

    private final DriverRepository driverRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EntityManager entityManager;

    public DriverService(DriverRepository driverRepository,
                         RedisTemplate<String, String> redisTemplate,
                         EntityManager entityManager) {
        this.driverRepository = driverRepository;
        this.redisTemplate = redisTemplate;
        this.entityManager = entityManager;
    }

    public Driver create(DriverCreateRequest request) {
        Driver driver = new Driver();
        driver.setName(request.name());
        driver.setEmail(request.email());
        driver.setPhone(request.phone());
        driver.setLicenseNumber(request.licenseNumber());
        driver.setStatus(DriverStatus.AVAILABLE);
        Driver saved = driverRepository.save(driver);
        evictAvailableDrivers();
        return saved;
    }

    public Driver get(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
    }

    public List<Driver> availableDrivers() {
        List<String> cachedIds = redisTemplate.opsForList().range(AVAILABLE_CACHE_KEY, 0, -1);
        if (cachedIds != null && !cachedIds.isEmpty()) {
            List<Long> ids = cachedIds.stream().map(Long::valueOf).toList();
            return driverRepository.findAllById(ids).stream()
                    .filter(driver -> driver.getStatus() == DriverStatus.AVAILABLE)
                    .toList();
        }

        List<Driver> drivers = driverRepository.findByStatusOrderById(DriverStatus.AVAILABLE);
        if (!drivers.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(AVAILABLE_CACHE_KEY,
                    drivers.stream().map(driver -> driver.getId().toString()).toList());
            redisTemplate.expire(AVAILABLE_CACHE_KEY, 30, TimeUnit.SECONDS);
        }
        return drivers;
    }

    @Transactional
    public Optional<Driver> reserveAvailableDriver() {
        @SuppressWarnings("unchecked")
        List<Driver> drivers = entityManager.createNativeQuery("""
                WITH selected AS (
                    SELECT id
                    FROM drivers
                    WHERE status = 'AVAILABLE'
                    ORDER BY id
                    LIMIT 1
                    FOR UPDATE SKIP LOCKED
                )
                UPDATE drivers
                SET status = 'BUSY'
                WHERE id IN (SELECT id FROM selected)
                RETURNING *
                """, Driver.class).getResultList();
        evictAvailableDrivers();
        return drivers.stream().findFirst();
    }

    public Driver updateStatus(Long id, DriverStatus status) {
        Driver driver = get(id);
        driver.setStatus(status);
        Driver saved = driverRepository.save(driver);
        evictAvailableDrivers();
        return saved;
    }

    private void evictAvailableDrivers() {
        redisTemplate.delete(AVAILABLE_CACHE_KEY);
    }
}

