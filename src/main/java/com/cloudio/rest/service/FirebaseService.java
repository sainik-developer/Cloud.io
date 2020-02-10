package com.cloudio.rest.service;

import com.cloudio.rest.exception.FirebaseException;
import com.cloudio.rest.repository.TokenStatsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class FirebaseService {

    private final AWSS3Services awss3Services;
    private final TokenStatsRepository tokenStatsRepository;
    private final ObjectMapper objectMapper;


    @Value("${amazonProperties.sensitive.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.sensitive.fileName}")
    private String fileName;
    @Value("${firebase.database.url}")
    private String firebaseDataBaseUrl;

    @PostConstruct
    public void init() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(awss3Services.getFile(bucketName, fileName).getObjectContent()))
                    .setDatabaseUrl(firebaseDataBaseUrl)
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String refreshFireBaseCustomToken(final String accountId) {
        try {
            return FirebaseAuth.getInstance().createCustomToken(accountId);
        } catch (final FirebaseAuthException e) {
            throw new FirebaseException(e.getMessage());
        }
    }

    public boolean revokeFireBaseCustomToken(final String accountId) {
        try {
            FirebaseAuth.getInstance().revokeRefreshTokens(accountId);
            return true;
        } catch (final FirebaseAuthException e) {
            return false;
        }
    }

    public Mono<Integer> sendNotification(final List<Pair<String, String>> uUIDAndtokens, final Map<String, String> data) {
        String payload = null;
        try {
            payload = objectMapper.writeValueAsString(data);
        } catch (final Exception ignored) {

        }
        final String finalPayload = payload == null ? "Contact developer for payload" : payload;
        return Mono.just(uUIDAndtokens)
                .flatMapMany(toks -> {
                    final MulticastMessage message = MulticastMessage.builder().putAllData(data).setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH).build()).addAllTokens(toks.stream().map(Pair::getKey).collect(Collectors.toList()))
                            .build();
                    try {
                        List<SendResponse> sendResponses = FirebaseMessaging.getInstance().sendMulticast(message).getResponses();
                        return Flux.zip(Flux.fromIterable(uUIDAndtokens), Flux.fromIterable(sendResponses))
                                .flatMap(objects -> tokenStatsRepository.findByNotificationId(objects.getT1().getKey())
                                        .map(tokenStatsDo -> {
                                            if (objects.getT2().getMessageId() != null) {
                                                tokenStatsDo.setStatus("DELIVERED");
                                                tokenStatsDo.setExtraReason(objects.getT2().getMessageId());
                                                tokenStatsDo.setPayload(finalPayload);
                                            } else if (objects.getT2().getException() != null) {
                                                tokenStatsDo.setStatus("FAILED");
                                                tokenStatsDo.setExtraReason(objects.getT2().getException().getMessage());
                                                tokenStatsDo.setPayload(finalPayload);
                                            } else {
                                                tokenStatsDo.setStatus("FAILED");
                                                tokenStatsDo.setExtraReason("debug it. contact developer");
                                                tokenStatsDo.setPayload(finalPayload);
                                            }
                                            return tokenStatsDo;
                                        })
                                        .map(tokenStatsRepository::save));
                    } catch (final FirebaseMessagingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collectList()
                .map(tokenStatsDOS -> tokenStatsDOS.size() == uUIDAndtokens.size() ? 1 : 0);
    }


}
