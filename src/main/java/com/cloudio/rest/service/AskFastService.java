package com.cloudio.rest.service;

import reactor.core.publisher.Mono;

public interface AskFastService {
    Mono<Boolean> doAuthAndSendSMS(final String phoneNumber, final String smsContent);

    Mono<Boolean> doAuthAndSendEmail(final String toEmailAddress, final String emailContent);
}
