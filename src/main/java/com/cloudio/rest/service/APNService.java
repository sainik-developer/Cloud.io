package com.cloudio.rest.service;

import com.cloudio.rest.entity.TokenStatsDO;
import com.cloudio.rest.repository.TokenStatsRepository;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.PushType;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;

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
     * TODO we have to convert when it comes into work @Akash you try to refactor
     * @param voipToken
     * @param stringObjectMap
     * @param notificationId
     */
    public void sendVOIPNotification(final String voipToken, final Map<String, Object> stringObjectMap, final UUID notificationId) {
        final ApnsPayloadBuilder apnsPayloadBuilder = new ApnsPayloadBuilder();
        stringObjectMap.forEach(apnsPayloadBuilder::addCustomProperty);
        final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
                apnsClient.sendNotification(new SimpleApnsPushNotification(TokenUtil.sanitizeTokenString(voipToken), apnsVOIPTopic,
                        apnsPayloadBuilder.buildWithDefaultMaximumLength(), null,
                        DeliveryPriority.IMMEDIATE, PushType.VOIP, null, notificationId));
        log.info("apns voip is sent . waiting for response for voip token {}", voipToken);
        sendNotificationFuture.addListener(future -> {
            if (future.isSuccess()) {
                final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = sendNotificationFuture.getNow();
                final TokenStatsDO tokenStatsDo = tokenStatsRepository.findByNotificationId(pushNotificationResponse.getApnsId().toString());
                if (tokenStatsDo != null) {
                    tokenStatsDo.setStatus(pushNotificationResponse.isAccepted() ? "DELIVERED" : pushNotificationResponse.getRejectionReason());
                    tokenStatsDo.setPayload(apnsPayloadBuilder.buildWithDefaultMaximumLength());
                }
                tokenStatsRepository.save(tokenStatsDo);
                log.info("apns voip is sent successfully for accountId {} and response is {}", voipToken, pushNotificationResponse.toString());
            } else {
                final TokenStatsDO tokenStatsDo = tokenStatsRepository.findByNotificationId(notificationId.toString());
                if (tokenStatsDo != null) {
                    tokenStatsDo.setStatus("FAILED");
                    tokenStatsDo.setExtraReason(future.cause().toString());
                    tokenStatsDo.setPayload(apnsPayloadBuilder.buildWithDefaultMaximumLength());
                }
                tokenStatsRepository.save(tokenStatsDo);
                log.info("apns voip is sent failed for accountId {} ", voipToken);
                future.cause().printStackTrace();
            }
        });
    }

    public Mono<Boolean> sendAlertNotification(final String apnsToken, final Map<String, Object> stringObjectMap, final UUID notificationId) {
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
        return Mono.create(monoSink -> {
            final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
                    apnsClient.sendNotification(new SimpleApnsPushNotification(TokenUtil.sanitizeTokenString(apnsToken), apnsALERTTopic,
                            apnsPayloadBuilder.buildWithDefaultMaximumLength(), null,
                            DeliveryPriority.IMMEDIATE, PushType.ALERT, null, notificationId));
            log.info("alert apns is sent . waiting for response for apns token {}", apnsToken);
            sendNotificationFuture.addListener(future -> {
                        if (future.isSuccess()) {
                            monoSink.success(future);
                        } else {
                            monoSink.success(new RuntimeException(future.cause().toString()));
                        }
                    }
            );
        }).map(o -> {
            if (o instanceof PushNotificationResponse) {
                final PushNotificationResponse pushNotificationResponse = (PushNotificationResponse) o;
                final TokenStatsDO tokenStatsDo = tokenStatsRepository.findByNotificationId(pushNotificationResponse.getApnsId().toString());
                if (tokenStatsDo != null) {
                    tokenStatsDo.setStatus(pushNotificationResponse.isAccepted() ? "DELIVERED" : pushNotificationResponse.getRejectionReason());
                    tokenStatsDo.setPayload(pushNotificationResponse.getPushNotification().getPayload());
                    tokenStatsRepository.save(tokenStatsDo);
                }
                log.info("alert apns is sent successfully for accountId {} and response is {}", apnsToken, pushNotificationResponse.toString());
                return pushNotificationResponse.isAccepted();
            } else {
                final Throwable throwable = (Throwable) o;
                final TokenStatsDO tokenStatsDo = tokenStatsRepository.findByNotificationId(notificationId.toString());
                if (tokenStatsDo != null) {
                    tokenStatsDo.setStatus("FAILED");
                    tokenStatsDo.setExtraReason(throwable.getMessage());
                    tokenStatsDo.setPayload(apnsPayloadBuilder.buildWithDefaultMaximumLength());
                    tokenStatsRepository.save(tokenStatsDo);
                }
                log.info("alert apns is sent failed for accountId {} ", apnsToken);
                throwable.printStackTrace();
                return false;
            }
        });
    }


    /****
     * TODO we have to convert when it comes into work @Akash you try to refactor
     * @param apnsToken
     * @param stringObjectMap
     * @param notificationId
     */
    public void sendSilentNotification(final String apnsToken, final Map<String, Object> stringObjectMap, final UUID notificationId) {
        final ApnsPayloadBuilder apnsPayloadBuilder = new ApnsPayloadBuilder();
        stringObjectMap.forEach(apnsPayloadBuilder::addCustomProperty);
        apnsPayloadBuilder.setContentAvailable(true);
        apnsPayloadBuilder.addCustomProperty("apns-push-type", "background");
        final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
                apnsClient.sendNotification(new SimpleApnsPushNotification(TokenUtil.sanitizeTokenString(apnsToken), apnsBackgroundTopic,
                        apnsPayloadBuilder.buildWithDefaultMaximumLength(), null,
                        DeliveryPriority.CONSERVE_POWER, PushType.BACKGROUND, null, notificationId));
        log.info("silent apns is sent . waiting for response for apns token {} with body {}", apnsToken, apnsPayloadBuilder.buildWithDefaultMaximumLength());
        sendNotificationFuture.addListener(future -> {
            if (future.isSuccess()) {
                final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = sendNotificationFuture.getNow();
                final TokenStatsDO tokenStatsDo = tokenStatsRepository.findByNotificationId(pushNotificationResponse.getApnsId().toString());
                if (tokenStatsDo != null) {
                    tokenStatsDo.setStatus(pushNotificationResponse.isAccepted() ? "DELIVERED" : pushNotificationResponse.getRejectionReason());
                    tokenStatsDo.setPayload(apnsPayloadBuilder.buildWithDefaultMaximumLength());
                }
                tokenStatsRepository.save(tokenStatsDo);
                log.info("silent apns is sent successfully for apns token {} and response is {}", apnsToken, pushNotificationResponse.toString());
            } else {
                final TokenStatsDO tokenStatsDo = tokenStatsRepository.findByNotificationId(notificationId.toString());
                if (tokenStatsDo != null) {
                    tokenStatsDo.setStatus("FAILED");
                    tokenStatsDo.setExtraReason(future.cause().toString());
                    tokenStatsDo.setPayload(apnsPayloadBuilder.buildWithDefaultMaximumLength());
                }
                tokenStatsRepository.save(tokenStatsDo);
                log.info("alert apns is sent failed for accountId {} ", apnsToken);
                future.cause().printStackTrace();
            }
        });
    }
}
