package com.cloudio.rest.service;

import com.cloudio.rest.entity.GroupDO;
import com.cloudio.rest.entity.TokenDO;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.GroupType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import com.cloudio.rest.repository.TokenRepository;
import com.turo.pushy.apns.PushType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class NotificationService {
    private final APNService apnService;
    private final TokenRepository tokenRepository;
    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;
    private final FirebaseService firebaseService;


    @Value("${app.maxRingTime}")
    private Integer maxRingTime;


//    public Mono<Boolean> sendNotification(final String token, final Map<String, Object> data) {
//        if (flag)
//            return Mono.just(true);
//        return Mono.just(false);
//    }

    public Mono<Boolean> sendNotificationToGroup(final GroupDO groupDO, final Map<String, Object> data) {
        return Mono.just(groupDO)
                .filter(groupDo -> groupDo.getGroupType() == GroupType.DEFAULT)
                .flatMap(groupDo -> companyRepository.findByCompanyId(groupDO.getCompanyId()))
                .flatMapMany(companyDO -> accountRepository.findByCompanyIdAndStatus(companyDO.getCompanyId(), AccountStatus.ACTIVE))
                .flatMap(accountDO -> tokenRepository.findByAccountId(accountDO.getAccountId()))
                .groupBy(TokenDO::getDevice)
                .flatMap(tokenDOGroupedFlux -> tokenDOGroupedFlux.collectList()
                        .flatMap(tokenDos -> tokenDOGroupedFlux.key().equals("ios") ? apnService.send(tokenDos, data) : firebaseService.send(tokenDos, data)))
                .switchIfEmpty(Mono.error(new RuntimeException("Only default group is supported now!")));


    }

        enum Type {
            ANDROID,
            IOS
        }
//
//        public void sendAlertNotification( final List<String> accountIds, final Map<String, Object> bodyData){
//            final Map<Type, List<Pair<String, String>>> typeListMap = getAPNSTokenByType(accountIds, PushType.ALERT, "alert");
//            if (!typeListMap.get(Type.ANDROID).isEmpty()) {
//                final NotificationModel firebaseNotificationModel = new NotificationModel();
//                firebaseNotificationModel.setData(bodyData);
//                firebaseNotificationModel.setPriority(NotificationModel.PushNotificationPriority.high);
//                firebaseNotificationModel.setRegistration_ids(typeListMap.get(Type.ANDROID).stream().map(Pair::getFirst).collect(Collectors.toList()));
//                log.info("sending notification to android devices {}", firebaseNotificationModel.getRegistration_ids());
//                firebaseService.sendFirebasePushNotification(firebaseNotificationModel, typeListMap.get(Type.ANDROID));
//            }
//            if (!typeListMap.get(Type.IOS).isEmpty()) {
//                typeListMap.get(Type.IOS).forEach(apnsTokenUUIDPair -> apnService.sendAlertNotification(apnsTokenUUIDPair.getFirst(), bodyData, UUID.fromString(apnsTokenUUIDPair.getSecond())));
//            }
//        }
//
//        public void sendRingNotification ( final List<String> accountIds, final String sessionId, String roomName,
//                String callerIdentifier,boolean isOneToOneRing, final String accountProfileImageUrl){
//            final Map<String, Object> bodyData = new HashMap<>();
//            bodyData.put("action", "ring");
//            bodyData.put("sessionId", sessionId);
//            bodyData.put("timeout", maxRingTime);
//            bodyData.put("roomName", roomName);
//            bodyData.put("callerIdentifier", callerIdentifier);
//            bodyData.put("oneToOne", isOneToOneRing);
//            bodyData.put("callerImageUrl", accountProfileImageUrl);
//            final Map<Type, List<Pair<String, String>>> typeListMap = getAPNSTokenByType(accountIds, PushType.VOIP, "ring");
//            if (!typeListMap.get(Type.ANDROID).isEmpty()) {
//                final NotificationModel firebaseNotificationModel = new NotificationModel();
//                firebaseNotificationModel.setData(bodyData);
//                firebaseNotificationModel.setPriority(NotificationModel.PushNotificationPriority.high);
//                firebaseNotificationModel.setRegistration_ids(typeListMap.get(Type.ANDROID).stream().map(Pair::getFirst).collect(Collectors.toList()));
//                firebaseService.sendFirebasePushNotification(firebaseNotificationModel, typeListMap.get(Type.ANDROID));
//            }
//            if (!typeListMap.get(Type.IOS).isEmpty()) {
//                typeListMap.get(Type.IOS).forEach(apnsTokenUUIDPair -> apnService.sendVOIPNotification(apnsTokenUUIDPair.getFirst(), bodyData, UUID.fromString(apnsTokenUUIDPair.getSecond())));
//            }
//        }
//
//        public void sendCancelNotification ( final List<String> accountIds, final String sessionId){
//            log.info("send cancel notification is called for sessionId {} to {}", sessionId, accountIds);
//            if (!accountIds.isEmpty()) {
//                final Map<String, Object> bodyData = new HashMap<>();
//                bodyData.put("action", "cancel");
//                bodyData.put("sessionId", sessionId);
//                sendSilentPushNotification(accountIds, bodyData);
//            }
//        }
//
//        private void sendSilentPushNotification ( final List<String> memberAccountIds,
//        final Map<String, Object> bodyData){
//            final Map<Type, List<Pair<String, String>>> typeListMap = getAPNSTokenByType(memberAccountIds, PushType.BACKGROUND, "cancel");
//            if (!typeListMap.get(Type.ANDROID).isEmpty()) {
//                NotificationModel firebaseNotificationModel = new NotificationModel();
//                firebaseNotificationModel.setData(bodyData);
//                firebaseNotificationModel.setPriority(NotificationModel.PushNotificationPriority.high);
//                firebaseNotificationModel.setRegistration_ids(typeListMap.get(Type.ANDROID).stream().map(Pair::getFirst).collect(Collectors.toList()));
//                log.info("sending notification to android devices: " + firebaseNotificationModel.getRegistration_ids());
//                firebaseService.sendFirebasePushNotification(firebaseNotificationModel, typeListMap.get(Type.ANDROID));
//            }
//            if (!typeListMap.get(Type.IOS).isEmpty()) {
//                typeListMap.get(Type.IOS).forEach(apnsTokenUUIDPair -> apnService.sendSilentNotification(apnsTokenUUIDPair.getFirst(), bodyData, UUID.fromString(apnsTokenUUIDPair.getSecond())));
//            }
//        }
//
//        private Map<Type, List<Pair<String, String>>> getAPNSTokenByType ( final List<String> accountIds,
//        final PushType pushType, final String actionType){
//            final List<FirebaseTokenDO> firebaseTokenDos = firebaseTokenRepository.findByAccountIds(accountIds);
//            if (accountIds.size() > firebaseTokenDos.size()) {
//                log.error("some account(s) does't(don't) have any firebasetoken entry");
//            } else {
//                log.info("all account has firebasetoken entry");
//            }
//            final List<String> withTokenAccountIds = firebaseTokenDos.stream().map(FirebaseTokenDO::getAccountId).collect(Collectors.toList());
//            final List<String> noTokenAccountIds = accountIds.stream().filter(s -> !withTokenAccountIds.contains(s)).collect(Collectors.toList());
//            final Map<Type, List<Pair<String, String>>> tokens = new HashMap<>();
//            final List<Pair<String, String>> iOS = new LinkedList<>();
//            final List<Pair<String, String>> android = new LinkedList<>();
//            final String requestBatchId = UUID.randomUUID().toString();
//            for (FirebaseTokenDO firebaseTokenDo : firebaseTokenDos) {
//                if (firebaseTokenDo.getDevice().equals("android")) {
//                    final TokenStatsDO tokenStatsDO = notificationStatsService.createStats(requestBatchId, firebaseTokenDo.getToken(),
//                            firebaseTokenDo.getAccountId(), actionType, "FIREBASE");
//                    android.add(Pair.of(firebaseTokenDo.getToken() == null ? "NULL" : firebaseTokenDo.getToken(), tokenStatsDO.getNotificationId()));
//                } else if (firebaseTokenDo.getDevice().equals("ios")) {
//                    if (pushType == PushType.ALERT || pushType == PushType.BACKGROUND) {
//                        final TokenStatsDO tokenStatsDO = notificationStatsService.createStats(requestBatchId, firebaseTokenDo.getToken(),
//                                firebaseTokenDo.getAccountId(), actionType, pushType.toString());
//                        iOS.add(Pair.of(firebaseTokenDo.getToken() == null ? "NULL" : firebaseTokenDo.getToken(), tokenStatsDO.getNotificationId()));
//                    } else if (pushType == PushType.VOIP) {
//                        final TokenStatsDO tokenStatsDO = notificationStatsService.createStats(requestBatchId, firebaseTokenDo.getToken(),
//                                firebaseTokenDo.getAccountId(), actionType, pushType.toString());
//                        iOS.add(Pair.of(firebaseTokenDo.getVoipToken() == null ? "NULL" : firebaseTokenDo.getVoipToken(), tokenStatsDO.getNotificationId()));
//                    }
//                }
//            }
//            noTokenAccountIds.forEach(accountId -> notificationStatsService.createStats(requestBatchId, null, accountId, actionType, pushType.toString()));
//            tokens.put(Type.IOS, iOS);
//            tokens.put(Type.ANDROID, android);
//            return tokens;
//        }
    }
