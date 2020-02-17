package com.cloudio.rest.service;

import com.cloudio.rest.dto.AskfastAdapterRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile(value = "prod")
public class AskFastProdService implements AskFastService {

    private final WebClient webClient;
    private final AskFastAuthService askFastAuthService;

    @Value("${askfast.base.url}")
    private String BASE_URL;

    @Value("${askfast.auth.url}")
    private String AUTH_URL;

    @Value("${askfast.start.dialog.url}")
    private String START_DIALOG_URL;

    @Value("${askfast.subaccount.url}")
    private String SUBACCOUNT_CREATE_URL;

    @Value("${askfast.subaccount.fetchkey.url}")
    private String KEY_URL;

    @Value("${askfast.root.accountId}")
    private String ROOT_ACCOUNT_ID;

    @Value("${askfast.root.refreshToken}")
    private String ROOT_REFRESH_TOKEN;

    private Mono<String> getRootAuthToken() {
        return askFastAuthService.fetchAuthToken(ROOT_ACCOUNT_ID, ROOT_REFRESH_TOKEN);
    }

    private Mono<Boolean> sendSms(final String phoneNumber, final String smsContent, final String accessToken) {
        return sendAdapterRequest(phoneNumber, smsContent, AskfastAdapterRequestDTO.AdapterType.SMS, accessToken)
                .doOnNext(aBoolean -> log.info("SMS is sent successfully"))
                .map(clientResponse -> clientResponse.statusCode() == org.springframework.http.HttpStatus.OK)
                .doOnError(throwable -> log.error("error while sending sms {}", throwable.getMessage()));
    }

    private Mono<ClientResponse> sendAdapterRequest(final String toAddress,
                                                    final String content,
                                                    final AskfastAdapterRequestDTO.AdapterType adapterType,
                                                    final String accessToken) {
        return webClient
                .post()
                .uri(BASE_URL + START_DIALOG_URL)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .header("Authorization", "Bearer " + accessToken)
                .syncBody(AskfastAdapterRequestDTO.builder()
                        .adapterType(adapterType)
                        .address(toAddress)
                        .url("text:// " + content)
                        .build())
                .exchange();
    }

    public Mono<Boolean> doAuthAndSendSMS(final String phoneNumber, final String smsContent) {
        return getRootAuthToken()
                .flatMap(s -> sendSms(phoneNumber, smsContent, s));
    }
}
