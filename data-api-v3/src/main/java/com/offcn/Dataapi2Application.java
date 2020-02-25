package com.offcn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.offcn.mapper")
public class Dataapi2Application {
    public static void main(String[] args) {

        SpringApplication.run(Dataapi2Application.class, args);
    }

}
