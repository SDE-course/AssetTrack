package com.assettrack.allocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AllocationApplication {
    public static void main(String[] args) {
        SpringApplication.run(AllocationApplication.class, args);
    }
}
