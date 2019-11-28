package com.cloudio.rest.service;

import com.amazonaws.util.StringUtils;
import com.cloudio.rest.dto.AccessTokenRequestDTO;
import com.cloudio.rest.dto.AskfastAdapterRequestDTO;
import com.cloudio.rest.dto.AskfastCreateSubAccountRequestDTO;
import com.cloudio.rest.dto.AskfastImpersonateSubAccountDTO;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.pojo.AskfastDetail;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

import java.time.LocalDateTime;
import java.util.Arrays;
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

    private final AtomicReference<String> CACHED_ACCESS_TOKEN = new AtomicReference<>(null);
    private final AtomicLong lastCachedTime = new AtomicLong(0);

    private Mono<String> getRootAuthToken() {
        return System.currentTimeMillis() - lastCachedTime.longValue() > 100000 || StringUtils.isNullOrEmpty(CACHED_ACCESS_TOKEN.get()) ? fetchAuthToken(ROOT_ACCOUNT_ID, ROOT_REFRESH_TOKEN) : Mono.just(CACHED_ACCESS_TOKEN.get());
    }

    private Mono<String> fetchAuthToken(final String accountId, final String refreshToken) {
        return webClient.post()
                .uri(BASE_URL + AUTH_URL)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .syncBody(AccessTokenRequestDTO.builder().accountId(accountId).refreshToken(refreshToken).build())
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
        return getRootAuthToken()
                .flatMap(s -> sendSms(phoneNumber, smsContent, s));
    }

    public Mono<Boolean> doAuthAndSendEmail(final String toEmailAddress, final String emailContent) {
        return getRootAuthToken()
                .flatMap(s -> sendEmail(toEmailAddress, emailContent, s));
    }

    public Mono<AskfastDetail> createMemberAccount(final AccountDO memberAccountDO, final AccountDO adminAccountDO, final String companyName) {
        log.info("Member account is going to be created on ask-fast platform");
        return fetchAuthToken(adminAccountDO.getAskfastDetail().getAccountId(), adminAccountDO.getAskfastDetail().getRefreshToken())
                .flatMap(companyAdminAuthToken -> createSubAccountAtAskFast(createSubAccountRequestDTO(memberAccountDO.getPhoneNumber() + "-" + companyName + "@cloud.io",
                        memberAccountDO.getPhoneNumber() + "-" + companyName + "@" + LocalDateTime.now(), memberAccountDO.getFirstName() + memberAccountDO.getLastName(),
                        memberAccountDO.getPhoneNumber(), memberAccountDO.getPhoneNumber() + "-" + companyName + "@cloud.io"), companyAdminAuthToken)
                        .doOnError(throwable -> log.error("Creating sub account failed due to " + throwable.getMessage()))
                        .doOnNext(subAccountId -> log.info("Creating sub account is successful and sub account id for member is {}", subAccountId))
                        .flatMap(subAccountId -> impersonateSubAccount(adminAccountDO.getAskfastDetail().getAccountId(), adminAccountDO.getAskfastDetail().getRefreshToken(), subAccountId, companyAdminAuthToken))
                        .doOnError(throwable -> log.error("impersonate account failed{}", throwable.getMessage()))
                        .doOnNext(subAccountAuthToken -> log.info("impersonate account successful, where token is {}", subAccountAuthToken)))
                .doOnNext(subAccountAuthToken -> log.info("sub account is created for member and auth token  is {}", subAccountAuthToken))
                .doOnNext(subAccountAuthToken -> log.info("going to retrieve accoutId and refresh token of member account with auth token {}", subAccountAuthToken))
                .flatMap(this::retrieveAskFastCredentialByAuthToken)
                .doOnNext(askfastDetail -> log.info("Retrieving the ask fast details completed successfully {}", askfastDetail))
                .switchIfEmpty(Mono.error(new RuntimeException()));
    }

    public Mono<AskfastDetail> createAdminAccount(final AccountDO accountDO, final String companyName) {
        log.info("Admin account is going to be created on ask-fast platform");
        return getRootAuthToken()
                .doOnNext(s -> log.info("Root account token is fetched successfully "))
                .flatMap(rootAccountAuthToken -> createSubAccountAtAskFast(createSubAccountRequestDTO(companyName + "@cloud.io",
                        "Welcome1$", companyName, accountDO.getPhoneNumber(), companyName + "@cloud.io"), rootAccountAuthToken)
                        .doOnError(throwable -> log.error("Creating sub account failed due to " + throwable.getMessage()))
                        .doOnNext(subAccountId -> log.info("Creating sub account is successful and sub account id for member is {}", subAccountId))
                        .flatMap(subAccountId -> impersonateSubAccount(ROOT_ACCOUNT_ID, ROOT_REFRESH_TOKEN, subAccountId, rootAccountAuthToken))
                        .doOnError(throwable -> log.error("impersonate account failed{}", throwable.getMessage()))
                        .doOnNext(subAccountAuthToken -> log.info("impersonate account successful, where token is {}", subAccountAuthToken)))
                .flatMap(this::retrieveAskFastCredentialByAuthToken)
                .switchIfEmpty(Mono.error(new RuntimeException()));
    }

    private Mono<String> createSubAccountAtAskFast(final AskfastCreateSubAccountRequestDTO requestDTO,
                                                   final String authToken) {
        return webClient.post()
                .uri(BASE_URL + SUBACCOUNT_CREATE_URL)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .header("Authorization", "Bearer " + authToken)
                .syncBody(requestDTO)
                .exchange()
                .filter(clientResponse -> clientResponse.statusCode() == org.springframework.http.HttpStatus.OK)
                .flatMap(clientResponse -> clientResponse.bodyToMono(ArrayNode.class))
                .map(jsonNodes -> jsonNodes.get(0).asText())
                .switchIfEmpty(Mono.error(new RuntimeException("Create sub account error where request is = " + requestDTO)));
    }

    private Mono<String> impersonateSubAccount(final String parentAccountId, final String parentRefreshToken, final String subAccountId, final String accessToken) {
        return webClient.post()
                .uri(BASE_URL + SUBACCOUNT_IMPERSONATE_URL)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .header("Authorization", "Bearer " + accessToken)
                .syncBody(AskfastImpersonateSubAccountDTO.builder().accountId(parentAccountId).refreshToken(parentRefreshToken).subAccountId(subAccountId).build())
                .exchange()
                .doOnError(throwable -> log.error("error while calling {}", throwable.getMessage()))
                .doOnNext(clientResponse -> log.info("ask-fast access token response is fetched successfully"))
                .filter(clientResponse -> clientResponse.statusCode() == org.springframework.http.HttpStatus.OK)
                .flatMap(clientResponse -> clientResponse.bodyToMono(ObjectNode.class))
                .map(objectNode -> objectNode.get("result"))
                .map(jsonNode -> jsonNode.get("accessToken").asText())
                .switchIfEmpty(Mono.error(new RuntimeException("impersonate Sub account error for sub account id " + subAccountId)));
    }

    private Mono<AskfastDetail> retrieveAskFastCredentialByAuthToken(final String authToken) {
        return webClient.get()
                .uri(BASE_URL + KEY_URL)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .header("Authorization", "Bearer " + authToken)
                .exchange()
                .filter(clientResponse -> clientResponse.statusCode() == org.springframework.http.HttpStatus.OK)
                .flatMap(clientResponse -> clientResponse.bodyToMono(AskfastDetail.class))
                .switchIfEmpty(Mono.error(new RuntimeException("retrieving sub account error")));
    }

    private AskfastCreateSubAccountRequestDTO createSubAccountRequestDTO(final String userName, final String password,
                                                                         final String name, final String phoneNumber,
                                                                         final String emailId) {
        return AskfastCreateSubAccountRequestDTO.builder().userName(userName).password(password).name(name)
                .contactInfos(Arrays.asList(AskfastCreateSubAccountRequestDTO.ContactInfo.builder()
                        .contactInfoTag(AskfastCreateSubAccountRequestDTO.ContactInfoTag.PHONE)
                        .contactInfo(phoneNumber).build(), AskfastCreateSubAccountRequestDTO.ContactInfo.builder()
                        .contactInfoTag(AskfastCreateSubAccountRequestDTO.ContactInfoTag.EMAIL)
                        .contactInfo(emailId).build())).build();
    }
}
