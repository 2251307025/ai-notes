package com.chj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AINotesApplication {
    public static void main(String[] args) {
        SpringApplication.run(AINotesApplication.class,args);
    }
}
