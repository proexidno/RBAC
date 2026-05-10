package com.example.taxi.notification.repository;

import com.example.taxi.notification.model.NotificationTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
    List<NotificationTask> findByTripIdOrderByCreatedAtAsc(Long tripId);
}

