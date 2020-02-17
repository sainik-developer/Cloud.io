package com.cloudio.rest.service;

import reactor.core.publisher.Mono;

public interface AskFastService {
    default Mono<Boolean> doAuthAndSendSMS(final String phoneNumber, final String smsContent) {
        return Mono.just(true);
    }

}
