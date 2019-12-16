package com.cloudio.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
