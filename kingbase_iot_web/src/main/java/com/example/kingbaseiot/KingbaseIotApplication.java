package com.example.kingbaseiot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KingbaseIotApplication {
    public static void main(String[] args) {
        SpringApplication.run(KingbaseIotApplication.class, args);
    }
}
