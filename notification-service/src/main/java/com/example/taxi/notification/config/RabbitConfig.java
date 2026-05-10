package com.example.taxi.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    Queue notificationQueue(@Value("${notification.rabbitmq.queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    DirectExchange notificationExchange(@Value("${notification.rabbitmq.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Binding notificationBinding(Queue notificationQueue,
                                DirectExchange notificationExchange,
                                @Value("${notification.rabbitmq.routing-key}") String routingKey) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(routingKey);
    }

    @Bean
    MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
