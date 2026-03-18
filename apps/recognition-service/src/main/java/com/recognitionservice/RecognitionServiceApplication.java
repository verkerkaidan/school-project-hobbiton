package com.recognitionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RecognitionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecognitionServiceApplication.class, args);
    }

}
