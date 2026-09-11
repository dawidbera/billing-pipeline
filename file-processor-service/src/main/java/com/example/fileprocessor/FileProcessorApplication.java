package com.example.fileprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FileProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileProcessorApplication.class, args);
    }
}
