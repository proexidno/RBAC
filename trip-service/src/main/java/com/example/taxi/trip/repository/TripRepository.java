package com.example.taxi.trip.repository;

import com.example.taxi.trip.model.Trip;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    @Query(value = """
            SELECT COUNT(*) AS "tripsCount", COALESCE(AVG(price), 0) AS "averagePrice"
            FROM trips
            WHERE created_at >= :start AND created_at < :end
            """, nativeQuery = true)
    DailyStatsProjection dailyStats(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    interface DailyStatsProjection {
        Long getTripsCount();

        BigDecimal getAveragePrice();
    }
}
