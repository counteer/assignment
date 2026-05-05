package com.hotel.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AssignmentApplication {

    static void main(String[] args) {
        SpringApplication.run(AssignmentApplication.class, args);
    }

}
