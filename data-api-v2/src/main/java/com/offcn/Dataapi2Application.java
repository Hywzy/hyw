package com.offcn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Dataapi2Application {
    public static void main(String[] args) {
        SpringApplication.run(Dataapi2Application.class, args);
    }

}
