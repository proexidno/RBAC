package com.example.taxi.notification.messaging;

import com.example.taxi.notification.dto.NotificationMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public NotificationPublisher(RabbitTemplate rabbitTemplate,
                                 @Value("${notification.rabbitmq.exchange}") String exchange,
                                 @Value("${notification.rabbitmq.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(Long taskId) {
        rabbitTemplate.convertAndSend(exchange, routingKey, new NotificationMessage(taskId));
    }
}
