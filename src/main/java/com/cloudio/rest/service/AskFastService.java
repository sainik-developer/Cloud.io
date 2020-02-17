package com.cloudio.rest.service;

import reactor.core.publisher.Mono;

public interface AskFastService {
    Mono<Boolean> doAuthAndSendSMS(final String phoneNumber, final String smsContent);
}
