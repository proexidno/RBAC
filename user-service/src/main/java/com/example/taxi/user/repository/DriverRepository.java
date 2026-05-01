package com.example.taxi.user.repository;

import com.example.taxi.user.model.Driver;
import com.example.taxi.user.model.DriverStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByStatusOrderById(DriverStatus status);
}

