package com.apigateway.apigateway.config;

import com.apigateway.apigateway.models.gym.Gym;
import com.apigateway.apigateway.models.reading.Reading;
import graphql.schema.TypeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQlConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .type("Post", builder -> builder
                        .typeResolver(env -> {
                            Object obj = env.getObject();

                            if (obj instanceof Gym) {
                                return env.getSchema().getObjectType("Gym");
                            }
                             if (obj instanceof Reading) {
                                 return env.getSchema().getObjectType("Reading");
                             }

                            return null;
                        })
                );
    }
}