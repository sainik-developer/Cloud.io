package com.cloudio.rest.service;

import com.amazonaws.util.StringUtils;
import com.cloudio.rest.dto.AccessTokenRequestDTO;
import com.cloudio.rest.dto.AskfastAdapterRequestDTO;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
@Service
@RequiredArgsConstructor
public class AskFastService {

    private final WebClient webClient;

    @Value("${askfast.base.url}")
    private String BASE_URL;

    @Value("${askfast.auth.url}")
    private String AUTH_URL;

    @Value("${start.dialog.url}")
    private String START_DIALOG_URL;

    @Value("${askfast.accountId}")
    private String ACCOUNT_ID;

    @Value("${askfast.refreshToken}")
    private String REFRESH_TOKEN;

    private final AtomicReference<String> CACHED_ACCESS_TOKEN = new AtomicReference<>(null);
    private final AtomicLong lastCachedTime = new AtomicLong(0);

    private Mono<String> getAccessToken() {
        if (System.currentTimeMillis() - lastCachedTime.longValue() > 100000 || StringUtils.isNullOrEmpty(CACHED_ACCESS_TOKEN.get())) {
            return webClient.post()
                    .uri(BASE_URL + AUTH_URL)
                    .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .syncBody(AccessTokenRequestDTO.builder().accountId(ACCOUNT_ID).refreshToken(REFRESH_TOKEN).build())
                    .exchange()
                    .doOnError(throwable -> log.error("error while calling {}", throwable.getMessage()))
                    .doOnNext(clientResponse -> log.info("ask-fast access token response is fetched successfully"))
                    .filter(clientResponse -> clientResponse.statusCode() == org.springframework.http.HttpStatus.OK)
                    .flatMap(clientResponse -> clientResponse.bodyToMono(ObjectNode.class))
                    .map(objectNode -> objectNode.get("result"))
                    .map(jsonNode -> jsonNode.get("accessToken").asText())
                    .doOnNext(accessToken -> {
                        lastCachedTime.set(System.currentTimeMillis());
                        CACHED_ACCESS_TOKEN.set(accessToken);
                    });
        }
        return Mono.just(CACHED_ACCESS_TOKEN.get());
    }

    private Mono<Boolean> sendSms(final String phoneNumber, final String smsContent, final String accessToken) {
        return sendAdapterRequest(phoneNumber, smsContent, AskfastAdapterRequestDTO.AdapterType.SMS, accessToken)
                .doOnNext(aBoolean -> log.info("SMS is sent successfully"))
                .map(clientResponse -> clientResponse.statusCode() == org.springframework.http.HttpStatus.OK)
                .doOnError(throwable -> log.error("error while sending sms {}", throwable.getMessage()));
    }

    public Mono<Boolean> sendEmail(final String toEmailAddress, final String emailContent, final String accessToken) {
        return sendAdapterRequest(toEmailAddress, emailContent, AskfastAdapterRequestDTO.AdapterType.EMAIL, accessToken)
                .doOnNext(aBoolean -> log.info("Email is sent successfully"))
                .map(clientResponse -> clientResponse.statusCode() == org.springframework.http.HttpStatus.OK)
                .doOnError(throwable -> log.error("error while sending email {}", throwable.getMessage()));
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
        return getAccessToken()
                .flatMap(s -> sendSms(phoneNumber, smsContent, s));
    }

    public Mono<Boolean> doAuthAndSendEmail(final String toEmailAddress, final String emailContent){
        return getAccessToken()
                .flatMap(s -> sendEmail(toEmailAddress, emailContent, s));
    }
}
