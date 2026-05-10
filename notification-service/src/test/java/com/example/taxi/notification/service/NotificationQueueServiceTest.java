package com.example.taxi.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.taxi.notification.dto.NotificationCreateRequest;
import com.example.taxi.notification.model.NotificationStatus;
import com.example.taxi.notification.model.NotificationTask;
import com.example.taxi.notification.model.RecipientType;
import com.example.taxi.notification.messaging.NotificationPublisher;
import com.example.taxi.notification.repository.NotificationTaskRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationQueueServiceTest {
    @Mock
    private NotificationTaskRepository repository;
    @Mock
    private NotificationPublisher publisher;

    @InjectMocks
    private NotificationQueueService queueService;

    @Test
    void createPersistsPendingTask() {
        when(repository.save(any(NotificationTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationTask task = queueService.create(new NotificationCreateRequest(
                10L, RecipientType.PASSENGER, 1L, "Trip assigned"));

        assertThat(task.getTripId()).isEqualTo(10L);
        assertThat(task.getRecipientType()).isEqualTo(RecipientType.PASSENGER);
        assertThat(task.getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void beginProcessingClaimsPendingTaskAndIncrementsAttempts() {
        NotificationTask task = task(5L, "Trip assigned", 1);
        task.setStatus(NotificationStatus.PENDING);
        when(repository.findById(5L)).thenReturn(Optional.of(task));

        Optional<NotificationTask> claimed = queueService.beginProcessing(5L);

        assertThat(claimed).contains(task);
        assertThat(task.getStatus()).isEqualTo(NotificationStatus.PROCESSING);
        assertThat(task.getAttempts()).isEqualTo(2);
    }

    @Test
    void markSentCompletesTask() {
        NotificationTask task = task(5L, "Trip assigned", 1);
        task.setStatus(NotificationStatus.PROCESSING);
        task.setErrorMessage("previous error");
        when(repository.findById(5L)).thenReturn(Optional.of(task));

        queueService.markSent(5L);

        assertThat(task.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(task.getErrorMessage()).isNull();
        assertThat(task.getProcessedAt()).isNotNull();
    }

    @Test
    void markFailedReturnsTaskToQueueBeforeMaxAttempts() {
        NotificationTask task = task(5L, "Trip assigned", 2);
        when(repository.findById(5L)).thenReturn(Optional.of(task));

        boolean shouldRetry = queueService.markFailed(5L, "temporary failure");

        assertThat(task.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(task.getErrorMessage()).isEqualTo("temporary failure");
        assertThat(shouldRetry).isTrue();
    }

    @Test
    void markFailedStopsRetryingAtThreeAttempts() {
        NotificationTask task = task(5L, "Trip assigned", 3);
        when(repository.findById(5L)).thenReturn(Optional.of(task));

        boolean shouldRetry = queueService.markFailed(5L, "permanent failure");

        assertThat(task.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(shouldRetry).isFalse();
    }

    private NotificationTask task(Long id, String message, int attempts) {
        NotificationTask task = new NotificationTask();
        ReflectionTestUtils.setField(task, "id", id);
        task.setTripId(10L);
        task.setRecipientType(RecipientType.DRIVER);
        task.setRecipientId(2L);
        task.setMessage(message);
        task.setAttempts(attempts);
        task.setStatus(NotificationStatus.PROCESSING);
        return task;
    }
}
