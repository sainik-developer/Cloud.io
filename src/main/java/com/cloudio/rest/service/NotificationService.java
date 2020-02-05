package com.cloudio.rest.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class NotificationService {

    public Mono<Boolean> sendNotification(final String device,final String token, final Map<String, Object> data) {
        return Mono.just(true);
    }

}
