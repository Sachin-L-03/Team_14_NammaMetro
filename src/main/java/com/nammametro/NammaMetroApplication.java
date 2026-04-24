package com.nammametro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Namma Metro Operations & Information Management System.
 *
 * @EnableScheduling enables Spring's scheduled task execution (used by ScheduledTaskService).
 *
 * SRP: This class has one responsibility — bootstrapping the Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling
public class NammaMetroApplication {

    public static void main(String[] args) {
        SpringApplication.run(NammaMetroApplication.class, args);
    }
}
