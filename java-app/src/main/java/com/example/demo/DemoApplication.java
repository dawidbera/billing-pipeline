package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Java Spring Boot Application.
 * Initializes the Spring context and starts the embedded Tomcat web server on port 8080.
 *
 * @author Antigravity
 * @version 1.0.0
 */
@SpringBootApplication
public class DemoApplication {

    /**
     * Main method that boots the Spring application framework.
     *
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
