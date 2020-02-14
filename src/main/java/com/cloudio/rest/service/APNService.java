package com.cloudio.rest.service;

import com.cloudio.rest.repository.TokenStatsRepository;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.PushType;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Log4j2
@Component
@RequiredArgsConstructor
public class APNService {

    private final ApnsClient apnsClient;
    private final TokenStatsRepository tokenStatsRepository;

    @Value("${apns.bundle.id}")
    private String apnsBundleId;

    private String apnsVOIPTopic;
    private String apnsALERTTopic;
    private String apnsBackgroundTopic;

    @PostConstruct
    private void managerTopic() {
        apnsVOIPTopic = apnsBundleId + ".voip";
        apnsALERTTopic = apnsBundleId;
        apnsBackgroundTopic = apnsBundleId;
    }

    /***
     * @param voipToken
     * @param data
     * @param notificationId
     */
    public Mono<Boolean> sendVOIPNotification(final String voipToken, final Map<String, String> data, final UUID notificationId) {
        final ApnsPayloadBuilder apnsPayloadBuilder = new ApnsPayloadBuilder();
        data.forEach(apnsPayloadBuilder::addCustomProperty);
        return Mono.create((Consumer<MonoSink<PushNotificationResponse<SimpleApnsPushNotification>>>) monoSink -> {
            final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
                    apnsClient.sendNotification(new SimpleApnsPushNotification(TokenUtil.sanitizeTokenString(voipToken), apnsVOIPTopic,
                            apnsPayloadBuilder.buildWithDefaultMaximumLength(), null,
                            DeliveryPriority.IMMEDIATE, PushType.VOIP, null, notificationId));
            sendNotificationFuture.addListener((GenericFutureListener<Future<PushNotificationResponse<SimpleApnsPushNotification>>>) future -> {
                if (future.isSuccess()) {
                    monoSink.success(future.getNow());
                } else {
                    monoSink.error(new RuntimeException(future.cause().toString()));
                }
            });
        })
                .doOnNext(response -> log.info("apns voip is sent . waiting for response for voip token {}", voipToken))
                .flatMap(apnsResponse -> tokenStatsRepository.findByNotificationId(apnsResponse.getApnsId().toString())
                        .map(tokenStatsDo -> {
                            tokenStatsDo.setStatus(apnsResponse.isAccepted() ? "DELIVERED" : apnsResponse.getRejectionReason());
                            tokenStatsDo.setPayload(apnsResponse.getPushNotification().getPayload());
                            return tokenStatsDo;
                        }).flatMap(tokenStatsRepository::save)
                        .map(tokenStatsDO -> apnsResponse.isAccepted()))
                .doOnError(throwable -> {
                    log.info("alert apns is sent failed for accountId {} ", voipToken);
                    throwable.printStackTrace();
                    tokenStatsRepository.findByNotificationId(notificationId.toString())
                            .map(tokenStatsDo -> {
                                tokenStatsDo.setStatus("FAILED");
                                tokenStatsDo.setExtraReason(throwable.getMessage());
                                tokenStatsDo.setPayload(createAlertBuilder(data).buildWithDefaultMaximumLength());
                                return tokenStatsDo;
                            })
                            .flatMap(tokenStatsRepository::save)
                            .subscribe();
                });
    }


    public Mono<Integer> sendAlertNotifications(final List<Pair<String, String>> uUIDAndtokens, final Map<String, String> data) {
        return Flux.fromIterable(uUIDAndtokens)
                .flatMap(uUIDAndtoken -> this.sendAlertNotification(uUIDAndtoken.getRight(), data, UUID.fromString(uUIDAndtoken.getLeft())))
                .collectList()
                .map(booleans -> booleans.stream().anyMatch(Boolean::booleanValue) ? 1 : 0);
    }

    public Mono<Boolean> sendAlertNotification(final String apnsToken, final Map<String, String> data, final UUID notificationId) {
        return Mono.create((Consumer<MonoSink<PushNotificationResponse<SimpleApnsPushNotification>>>) monoSink -> {
            final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
                    apnsClient.sendNotification(new SimpleApnsPushNotification(TokenUtil.sanitizeTokenString(apnsToken), apnsALERTTopic,
                            createAlertBuilder(data).buildWithDefaultMaximumLength(), null,
                            DeliveryPriority.IMMEDIATE, PushType.ALERT, null, notificationId));
            log.info("alert apns is sent . waiting for response for apns token {}", apnsToken);
            sendNotificationFuture.addListener((GenericFutureListener<Future<PushNotificationResponse<SimpleApnsPushNotification>>>) future -> {
                if (future.isSuccess()) {
                    monoSink.success(future.getNow());
                } else {
                    monoSink.error(new RuntimeException(future.cause().toString()));
                }
            });
        })
                .doOnNext(response -> log.info("alert apns is sent successfully for accountId {} and response is {}", apnsToken, response.toString()))
                .flatMap(apnsResponse -> tokenStatsRepository.findByNotificationId(apnsResponse.getApnsId().toString())
                        .map(tokenStatsDo -> {
                            tokenStatsDo.setStatus(apnsResponse.isAccepted() ? "DELIVERED" : apnsResponse.getRejectionReason());
                            tokenStatsDo.setPayload(apnsResponse.getPushNotification().getPayload());
                            return tokenStatsDo;
                        })
                        .flatMap(tokenStatsRepository::save)
                        .map(tokenStatsDO -> apnsResponse.isAccepted()))
                .doOnError(throwable -> {
                    log.info("alert apns is sent failed for accountId {} ", apnsToken);
                    throwable.printStackTrace();
                    tokenStatsRepository.findByNotificationId(notificationId.toString())
                            .map(tokenStatsDo -> {
                                tokenStatsDo.setStatus("FAILED");
                                tokenStatsDo.setExtraReason(throwable.getMessage());
                                tokenStatsDo.setPayload(createAlertBuilder(data).buildWithDefaultMaximumLength());
                                return tokenStatsDo;
                            })
                            .flatMap(tokenStatsRepository::save)
                            .subscribe();
                });
    }

    private ApnsPayloadBuilder createAlertBuilder(final Map<String, String> stringObjectMap) {
        final ApnsPayloadBuilder apnsPayloadBuilder = new ApnsPayloadBuilder();
        apnsPayloadBuilder.setAlertTitle((String) stringObjectMap.getOrDefault("title", "no title"));
        apnsPayloadBuilder.setAlertBody((String) stringObjectMap.getOrDefault("body", "no body"));
        apnsPayloadBuilder.setAlertSubtitle((String) stringObjectMap.getOrDefault("subtitle", null));
        apnsPayloadBuilder.setSound((String) stringObjectMap.getOrDefault("sound", "default"));
        stringObjectMap.forEach((key, value) -> {
            if (!key.equals("title") && !key.equals("body") && !key.equals("subtitle") && !key.equals("sound")) {
                apnsPayloadBuilder.addCustomProperty(key, value);
            }
        });
        return apnsPayloadBuilder;
    }


    /****
     * @param apnsToken
     * @param data
     * @param notificationId
     */
    public Mono<Boolean> sendSilentNotification(final String apnsToken, final Map<String, String> data, final UUID notificationId) {
        final ApnsPayloadBuilder apnsPayloadBuilder = new ApnsPayloadBuilder();
        data.forEach(apnsPayloadBuilder::addCustomProperty);
        apnsPayloadBuilder.setContentAvailable(true);
        apnsPayloadBuilder.addCustomProperty("apns-push-type", "background");
        return Mono.create((Consumer<MonoSink<PushNotificationResponse<SimpleApnsPushNotification>>>) monoSink -> {
            final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
                    apnsClient.sendNotification(new SimpleApnsPushNotification(TokenUtil.sanitizeTokenString(apnsToken), apnsBackgroundTopic,
                            apnsPayloadBuilder.buildWithDefaultMaximumLength(), null,
                            DeliveryPriority.CONSERVE_POWER, PushType.BACKGROUND, null, notificationId));
            log.info("silent apns is sent . waiting for response for apns token {} with body {}", apnsToken, apnsPayloadBuilder.buildWithDefaultMaximumLength());
            sendNotificationFuture.addListener((GenericFutureListener<Future<PushNotificationResponse<SimpleApnsPushNotification>>>) future -> {
                if (future.isSuccess()) {
                    monoSink.success(future.getNow());
                } else {
                    monoSink.error(new RuntimeException(future.cause().toString()));
                }
            });
        })
                .doOnNext(response -> log.info("silent apns is sent successfully for apns token {} and response is {}", apnsToken, response.toString()))
                .flatMap(apnsResponse -> tokenStatsRepository.findByNotificationId(apnsResponse.getApnsId().toString())
                        .map(tokenStatsDo -> {
                            tokenStatsDo.setStatus(apnsResponse.isAccepted() ? "DELIVERED" : apnsResponse.getRejectionReason());
                            tokenStatsDo.setPayload(apnsResponse.getPushNotification().getPayload());
                            return tokenStatsDo;
                        }).flatMap(tokenStatsRepository::save)
                        .map(tokenStatsDO -> apnsResponse.isAccepted()))
                .doOnError(throwable -> {
                    log.info("alert apns is sent failed for accountId {} ", apnsToken);
                    throwable.printStackTrace();
                    tokenStatsRepository.findByNotificationId(notificationId.toString())
                            .map(tokenStatsDo -> {
                                tokenStatsDo.setStatus("FAILED");
                                tokenStatsDo.setExtraReason(throwable.getMessage());
                                tokenStatsDo.setPayload(createAlertBuilder(data).buildWithDefaultMaximumLength());
                                return tokenStatsDo;
                            })
                            .flatMap(tokenStatsRepository::save)
                            .subscribe();
                });
    }
}
