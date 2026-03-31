package com.libraryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Zorg dat de keys als normale tekst (Strings) worden opgeslagen
        template.setKeySerializer(new StringRedisSerializer());

        // Zorg dat de Book objecten als JSON worden opgeslagen (belangrijk!)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}