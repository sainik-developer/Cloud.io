package com.cloudio.rest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile(value = {"sandbox", "local"})
public class AskFastSandboxService implements AskFastService {

    @Value("${askfast.base.url}")
    private String BASE_URL;

    @Value("${askfast.auth.url}")
    private String AUTH_URL;

    @Value("${askfast.start.dialog.url}")
    private String START_DIALOG_URL;

    @Value("${askfast.subaccount.url}")
    private String SUBACCOUNT_CREATE_URL;

    @Value("${askfast.subaccount.impersonate.url}")
    private String SUBACCOUNT_IMPERSONATE_URL;

    @Value("${askfast.subaccount.fetchkey.url}")
    private String KEY_URL;

    @Value("${askfast.root.accountId}")
    private String ROOT_ACCOUNT_ID;

    @Value("${askfast.root.refreshToken}")
    private String ROOT_REFRESH_TOKEN;


    public Mono<Boolean> doAuthAndSendSMS(final String phoneNumber, final String smsContent) {
        return Mono.just(true);
    }

    public Mono<Boolean> doAuthAndSendEmail(final String toEmailAddress, final String emailContent) {
        return Mono.just(true);
    }
}
