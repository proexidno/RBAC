package com.example.taxi.notification.messaging;

import com.example.taxi.notification.dto.NotificationMessage;
import com.example.taxi.notification.model.NotificationTask;
import com.example.taxi.notification.service.NotificationQueueService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationQueueService queueService;
    private final NotificationPublisher publisher;
    private final long sendDelayMs;

    public NotificationListener(NotificationQueueService queueService,
            NotificationPublisher publisher,
            @Value("${notification.send-delay-ms}") long sendDelayMs) {
        this.queueService = queueService;
        this.publisher = publisher;
        this.sendDelayMs = sendDelayMs;
    }

    @RabbitListener(queues = "${notification.rabbitmq.queue}")
    public void consume(NotificationMessage message) {
        Optional<NotificationTask> taskOptional = queueService.beginProcessing(message.taskId());
        if (taskOptional.isEmpty()) {
            return;
        }

        NotificationTask task = taskOptional.get();
        try {
            log.info("Sending notification {} to {} {}: {}",
                    task.getId(), task.getRecipientType(), task.getRecipientId(), task.getMessage());
            sleep(sendDelayMs);
            if (task.getMessage().contains("FAIL")) {
                throw new IllegalStateException("Simulated notification failure");
            }
            queueService.markSent(task.getId());
        } catch (RuntimeException ex) {
            boolean shouldRetry = queueService.markFailed(task.getId(), ex.getMessage());
            if (shouldRetry) {
                publisher.publish(task.getId());
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
