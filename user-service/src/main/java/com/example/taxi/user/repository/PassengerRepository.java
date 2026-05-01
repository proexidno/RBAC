package com.example.taxi.user.repository;

import com.example.taxi.user.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
}

