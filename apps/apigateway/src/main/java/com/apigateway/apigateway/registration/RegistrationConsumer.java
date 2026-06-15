package com.apigateway.apigateway.registration;

import com.apigateway.apigateway.clients.AuthClient;
import com.apigateway.apigateway.clients.ProfileClient;
import com.apigateway.apigateway.config.RabbitMQConfig;
import com.apigateway.apigateway.models.AuthenticationResponse;
import com.apigateway.apigateway.models.RegistrationResponse;
import com.apigateway.apigateway.models.ProfileResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import feign.FeignException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RegistrationConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(RegistrationConsumer.class);

    private final AuthClient authClient;
    private final ProfileClient profileClient;

    public RegistrationConsumer(
            AuthClient authClient,
            ProfileClient profileClient) {

        this.authClient = authClient;
        this.profileClient = profileClient;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public RegistrationResponse consume(RegistrationMessage message) {
        AuthenticationResponse auth = null;
        try {
            logger.info(
                    "Processing registration {}",
                    message.getRegistrationId()
            );

            auth = authClient.createAuthentication(
                    message.getRequest()
                            .toAuthenticationRequest()
            );

            ProfileResponse profile =
                    profileClient.createProfile(
                            message.getRequest()
                                    .toProfileRequest(),
                            "Bearer " + auth.getAccessToken()
                    );

            logger.info(
                    "Registration completed {}",
                    message.getRegistrationId()
            );

            return new RegistrationResponse(
                    profile,
                    auth.getAccessToken(),
                    auth.getUserId()
            );

        } catch (FeignException.BadRequest ex) {

            logger.warn(
                    "Bad request during registration {}",
                    message.getRegistrationId()
            );

            return errorResponse(
                    "BAD_REQUEST",
                    "Invalid registration data or user already exists."
            );

        } catch (FeignException.Unauthorized ex) {

            logger.warn(
                    "Unauthorized during registration {}",
                    message.getRegistrationId()
            );

            return errorResponse(
                    "UNAUTHORIZED",
                    "Authentication failed."
            );

        } catch (FeignException.Forbidden ex) {

            logger.warn(
                    "Forbidden during registration {}",
                    message.getRegistrationId()
            );

            return errorResponse(
                    "FORBIDDEN",
                    "Access denied."
            );

        } catch (FeignException.NotFound ex) {

            logger.warn(
                    "Resource not found during registration {}",
                    message.getRegistrationId()
            );

            return errorResponse(
                    "NOT_FOUND",
                    "Required service resource was not found."
            );

        } catch (FeignException.Conflict ex) {

            logger.warn(
                    "Conflict during registration {}",
                    message.getRegistrationId()
            );

            return errorResponse(
                    "CONFLICT",
                    "Resource already exists."
            );

        } catch (FeignException.ServiceUnavailable ex) {

            logger.error(
                    "Service unavailable during registration {}",
                    message.getRegistrationId()
            );

            rollback(auth);

            return errorResponse(
                    "SERVICE_UNAVAILABLE",
                    "A required service is currently unavailable."
            );

        } catch (FeignException.GatewayTimeout ex) {

            logger.error(
                    "Gateway timeout during registration {}",
                    message.getRegistrationId()
            );

            rollback(auth);

            return errorResponse(
                    "GATEWAY_TIMEOUT",
                    "A required service timed out."
            );

        } catch (FeignException.InternalServerError ex) {

            logger.error(
                    "Downstream service error during registration {}",
                    message.getRegistrationId(),
                    ex
            );

            rollback(auth);

            return errorResponse(
                    "DOWNSTREAM_ERROR",
                    "A downstream service encountered an error."
            );

        } catch (FeignException ex) {

            logger.error(
                    "Feign error during registration {}",
                    message.getRegistrationId(),
                    ex
            );

            rollback(auth);

            return errorResponse(
                    "FEIGN_ERROR",
                    "An external service request failed."
            );

        } catch (Exception ex) {

            logger.error(
                    "Unexpected registration failure {}",
                    message.getRegistrationId(),
                    ex
            );

            rollback(auth);

            return errorResponse(
                    "INTERNAL_ERROR",
                    "An unexpected error occurred."
            );
        }
    }

    private void rollback(AuthenticationResponse auth) {

        if (auth == null) {
            return;
        }

        try {

            authClient.deleteAuthentication(
                    auth.getUserId()
            );

            logger.info(
                    "Rollback successful for user {}",
                    auth.getUserId()
            );

        } catch (Exception rollbackEx) {

            logger.error(
                    "Rollback failed for {}",
                    auth.getUserId(),
                    rollbackEx
            );
        }
    }

    private RegistrationResponse errorResponse(
            String code,
            String message) {

        RegistrationResponse response =
                new RegistrationResponse();

        response.setSuccess(false);
        response.setErrorCode(code);
        response.setErrorMessage(message);

        return response;
    }
}
