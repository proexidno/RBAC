package com.example.taxi.notification.service;

import com.example.taxi.notification.dto.NotificationCreateRequest;
import com.example.taxi.notification.messaging.NotificationPublisher;
import com.example.taxi.notification.model.NotificationStatus;
import com.example.taxi.notification.model.NotificationTask;
import com.example.taxi.notification.repository.NotificationTaskRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationQueueService {
    private final NotificationTaskRepository repository;
    private final NotificationPublisher publisher;

    public NotificationQueueService(NotificationTaskRepository repository, NotificationPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public NotificationTask create(NotificationCreateRequest request) {
        NotificationTask task = new NotificationTask();
        task.setTripId(request.tripId());
        task.setRecipientType(request.recipientType());
        task.setRecipientId(request.recipientId());
        task.setMessage(request.message());
        task.setStatus(NotificationStatus.PENDING);
        NotificationTask saved = repository.save(task);
        try {
            publisher.publish(saved.getId());
        } catch (RuntimeException ex) {
            saved.setStatus(NotificationStatus.FAILED);
            saved.setErrorMessage("Queue publish failed: " + ex.getMessage());
            repository.save(saved);
            throw ex;
        }
        return saved;
    }

    public List<NotificationTask> byTrip(Long tripId) {
        return repository.findByTripIdOrderByCreatedAtAsc(tripId);
    }

    @Transactional
    public Optional<NotificationTask> beginProcessing(Long id) {
        Optional<NotificationTask> taskOptional = repository.findById(id);
        if (taskOptional.isEmpty()) {
            return Optional.empty();
        }

        NotificationTask task = taskOptional.get();
        if (task.getStatus() != NotificationStatus.PENDING || task.getAttempts() >= 3) {
            return Optional.empty();
        }

        task.setStatus(NotificationStatus.PROCESSING);
        task.setAttempts(task.getAttempts() + 1);
        return Optional.of(task);
    }

    @Transactional
    public void markSent(Long id) {
        NotificationTask task = repository.findById(id).orElseThrow();
        task.setStatus(NotificationStatus.SENT);
        task.setErrorMessage(null);
        task.setProcessedAt(OffsetDateTime.now());
    }

    @Transactional
    public boolean markFailed(Long id, String errorMessage) {
        NotificationTask task = repository.findById(id).orElseThrow();
        task.setErrorMessage(errorMessage);
        task.setStatus(task.getAttempts() >= 3 ? NotificationStatus.FAILED : NotificationStatus.PENDING);
        return task.getStatus() == NotificationStatus.PENDING;
    }
}
