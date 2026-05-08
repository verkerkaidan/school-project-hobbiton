package com.profileconnectionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProfileConnectionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileConnectionServiceApplication.class, args);
    }
}
