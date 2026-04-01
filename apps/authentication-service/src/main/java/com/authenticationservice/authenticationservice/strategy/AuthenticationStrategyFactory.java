package com.authenticationservice.authenticationservice.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AuthenticationStrategyFactory {

    private final Map<AuthType, AuthenticationStrategy> strategyMap;

    public AuthenticationStrategyFactory(List<AuthenticationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        AuthenticationStrategy::getSupportedType,
                        Function.identity()
                ));
    }

    public AuthenticationStrategy getStrategy(AuthType type) {
        AuthenticationStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No AuthenticationStrategy registered for type: " + type
            );
        }
        return strategy;
    }
}
