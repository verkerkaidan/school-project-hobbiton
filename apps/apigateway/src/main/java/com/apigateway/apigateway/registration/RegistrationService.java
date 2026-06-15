package com.apigateway.apigateway.registration;

import com.apigateway.apigateway.config.RabbitMQConfig;
import com.apigateway.apigateway.models.*;
import com.apigateway.apigateway.clients.ProfileClient;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final RabbitTemplate rabbitTemplate;
    private final ProfileClient profileClient;

    public RegistrationService(RabbitTemplate rabbitTemplate, ProfileClient profileClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.profileClient = profileClient;
    }

    public RegistrationResponse submitRegistration(
            RegistrationRequest request) {

        return (RegistrationResponse)
                rabbitTemplate.convertSendAndReceive(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.ROUTING_KEY,
                        new RegistrationMessage(
                                UUID.randomUUID(),
                                request
                        )
                );
    }
}