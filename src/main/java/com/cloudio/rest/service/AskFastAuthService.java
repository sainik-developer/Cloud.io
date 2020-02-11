package com.cloudio.rest.service;

import com.cloudio.rest.dto.AccessTokenRequestDTO;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class AskFastAuthService {
    private final WebClient webClient;

    @Value("${askfast.base.url}")
    private String BASE_URL;

    @Value("${askfast.auth.url}")
    private String AUTH_URL;

    @Cacheable(value = "auth")
    public Mono<String> fetchAuthToken(final String accountId, final String refreshToken) {
        return webClient.post()
                .uri(BASE_URL + AUTH_URL)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .syncBody(AccessTokenRequestDTO.builder().accountId(accountId).refreshToken(refreshToken).build())
                .exchange()
                .doOnError(throwable -> log.error("error while calling {}", throwable.getMessage()))
                .doOnNext(clientResponse -> log.info("ask-fast access token response is fetched successfully for accountId {} and refreshtoken {}", accountId, refreshToken))
                .filter(clientResponse -> clientResponse.statusCode() == org.springframework.http.HttpStatus.OK)
                .flatMap(clientResponse -> clientResponse.bodyToMono(ObjectNode.class))
                .map(objectNode -> objectNode.get("result"))
                .map(jsonNode -> jsonNode.get("accessToken").asText()).cache();
    }
}
