package com.popcoclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PopcoClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(PopcoClientApplication.class, args);
    }

}
