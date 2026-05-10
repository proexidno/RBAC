package com.example.taxi.notification.messaging;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taxi.notification.dto.NotificationMessage;
import com.example.taxi.notification.model.NotificationStatus;
import com.example.taxi.notification.model.NotificationTask;
import com.example.taxi.notification.model.RecipientType;
import com.example.taxi.notification.service.NotificationQueueService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {
    @Mock
    private NotificationQueueService queueService;
    @Mock
    private NotificationPublisher publisher;

    @Test
    void consumeMarksTaskAsSentAfterSuccessfulProcessing() {
        NotificationTask task = task(7L, "Trip assigned");
        NotificationListener listener = new NotificationListener(queueService, publisher, 0);
        when(queueService.beginProcessing(7L)).thenReturn(Optional.of(task));

        listener.consume(new NotificationMessage(7L));

        verify(queueService).markSent(7L);
        verify(publisher, never()).publish(7L);
    }

    @Test
    void consumeRepublishesTaskWhenRetryIsNeeded() {
        NotificationTask task = task(8L, "FAIL this notification");
        NotificationListener listener = new NotificationListener(queueService, publisher, 0);
        when(queueService.beginProcessing(8L)).thenReturn(Optional.of(task));
        when(queueService.markFailed(8L, "Simulated notification failure")).thenReturn(true);

        listener.consume(new NotificationMessage(8L));

        verify(queueService).markFailed(8L, "Simulated notification failure");
        verify(publisher).publish(8L);
    }

    @Test
    void consumeDoesNothingForUnknownTask() {
        NotificationListener listener = new NotificationListener(queueService, publisher, 0);
        when(queueService.beginProcessing(9L)).thenReturn(Optional.empty());

        listener.consume(new NotificationMessage(9L));

        verify(queueService, never()).markSent(9L);
        verify(queueService, never()).markFailed(anyLong(), anyString());
    }

    private NotificationTask task(Long id, String message) {
        NotificationTask task = new NotificationTask();
        ReflectionTestUtils.setField(task, "id", id);
        task.setTripId(10L);
        task.setRecipientType(RecipientType.PASSENGER);
        task.setRecipientId(1L);
        task.setMessage(message);
        task.setStatus(NotificationStatus.PROCESSING);
        task.setAttempts(1);
        return task;
    }
}
