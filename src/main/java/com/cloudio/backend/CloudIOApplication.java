package com.cloudio.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication

public class CloudIOApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudIOApplication.class, args);
    }

}
