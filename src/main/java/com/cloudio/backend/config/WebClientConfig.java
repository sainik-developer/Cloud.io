package com.cloudio.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient getWebClient() {
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector())
                .build();
    }
}
