package com.example.autoreview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AutoReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoReviewApplication.class, args);
    }
}
