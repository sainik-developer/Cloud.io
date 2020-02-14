package com.cloudio.rest.service;

import reactor.core.publisher.Mono;


public interface AdapterService {
    Mono<String> getAvailableAdapter();
}
