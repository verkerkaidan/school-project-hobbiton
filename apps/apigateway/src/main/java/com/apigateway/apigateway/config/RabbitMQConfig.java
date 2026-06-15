package com.apigateway.apigateway.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "registration.exchange";
    public static final String QUEUE = "registration.queue";
    public static final String ROUTING_KEY = "registration.created";

    @Bean
    public TopicExchange registrationExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue registrationQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding registrationBinding(
            Queue registrationQueue,
            TopicExchange registrationExchange) {

        return BindingBuilder
                .bind(registrationQueue)
                .to(registrationExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}