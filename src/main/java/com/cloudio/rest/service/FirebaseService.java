package com.cloudio.rest.service;

import com.cloudio.rest.entity.TokenStatsDO;
import com.cloudio.rest.exception.FirebaseException;
import com.cloudio.rest.repository.TokenStatsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.*;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
        } catch (final Exception e) {

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
                                .map(objects -> {
                                    final TokenStatsDO tokenStatsDO = tokenStatsRepository.findByNotificationId(objects.getT1().getKey());
                                    if (objects.getT2().getMessageId() != null) {
                                        tokenStatsDO.setStatus("DELIVERED");
                                        tokenStatsDO.setExtraReason(objects.getT2().getMessageId());
                                        tokenStatsDO.setPayload(finalPayload);
                                    } else if (objects.getT2().getException() != null) {
                                        tokenStatsDO.setStatus("FAILED");
                                        tokenStatsDO.setExtraReason(objects.getT2().getException().getMessage());
                                        tokenStatsDO.setPayload(finalPayload);
                                    } else {
                                        tokenStatsDO.setStatus("FAILED");
                                        tokenStatsDO.setExtraReason("debug it. contact developer");
                                        tokenStatsDO.setPayload(finalPayload);
                                    }
                                    return tokenStatsDO;
                                });

                    } catch (final FirebaseMessagingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(tokenStatsRepository::save)
                .collectList()
                .map(tokenStatsDOS -> tokenStatsDOS.size() == uUIDAndtokens.size() ? 1 : 0);
    }


}
