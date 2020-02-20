package com.cloudio.rest.service;

import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.entity.GroupDO;
import com.cloudio.rest.exception.AccountNotExistException;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.GroupType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import com.cloudio.rest.repository.TokenRepository;
import com.turo.pushy.apns.PushType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
@Service
public class NotificationService {
    private final APNService apnService;
    private final TokenRepository tokenRepository;
    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;
    private final FirebaseService firebaseService;
    private final NotificationStatsService notificationStatsService;


    //    public Mono<Boolean> sendNotification(final String token, final Map<String, Object> data) {
//        if (flag)
//            return Mono.just(true);
//        return Mono.just(false);
//    }
//    public Mono<Boolean> sendNotificationToAccount(final TokenDO tokenDO, final Map<String, String> data) {
//        return Mono.just(tokenDO.getDevice().equals("ios") ? apnsService.send() : firebaseService.sendNotificationToAccount(tokenDO.getToken(), data))
//                .switchIfEmpty(new RuntimeException());
//    }
//
    public Mono<Boolean> sendNotificationToGroup(final GroupDO groupDO, final Map<String, String> data, final String senderAccountId) {
        return Mono.just(groupDO)
                .filter(groupDo -> groupDo.getGroupType() == GroupType.DEFAULT)
                .flatMap(groupDo -> companyRepository.findByCompanyId(groupDO.getCompanyId()))
                .flatMap(companyDO -> accountRepository.findByCompanyIdAndStatus(companyDO.getCompanyId(), AccountStatus.ACTIVE)
                        .filter(accountDo -> !accountDo.getAccountId().equals(senderAccountId))
                        .map(AccountDO::getAccountId)
                        .collectList()
                        .flatMapMany(accountIds -> sendAlertNotification(accountIds, data))
                        .collectList()
                        .map(integers -> integers.stream().anyMatch(integer -> integer == 1))
                )
                .switchIfEmpty(Mono.error(new AccountNotExistException()));
    }

    enum OSType {
        ANDROID,
        IOS
    }

    public Flux<Integer> sendAlertNotification(final List<String> accountIds, final Map<String, String> bodyData) {
        log.info("alert send notification called for accounts {} and data is {}", accountIds, bodyData);
        return groupBy(accountIds, PushType.ALERT, "alert")
                .groupBy(Triple::getLeft)
                .flatMap(osTypeTripleGroupedFlux -> osTypeTripleGroupedFlux.collectList()
                        .flatMap(triples ->
                                osTypeTripleGroupedFlux.key() == OSType.ANDROID ? firebaseService.sendNotification(triples.stream().map(triple -> Pair.of(triple.getRight(), triple.getMiddle())).collect(Collectors.toList()), bodyData) :
                                        apnService.sendAlertNotifications(triples.stream().map(triple -> Pair.of(triple.getRight(), triple.getMiddle())).collect(Collectors.toList()), bodyData))
                );
    }

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

    private Flux<Triple<OSType, String, String>> groupBy(final List<String> accountIds, final PushType pushType,
                                                         final String actionType) {
        final String requestBatchId = UUID.randomUUID().toString();
        return Flux.fromIterable(accountIds)
                .flatMap(accountId -> tokenRepository.findByAccountId(accountId)
                        .flatMap(tokenDo -> {
                            if (tokenDo.getDevice().equals("android")) {
                                return notificationStatsService.createStats(requestBatchId, tokenDo.getToken(), tokenDo.getAccountId(), actionType, "FIREBASE")
                                        .map(tokenStatsDo -> Triple.of(OSType.ANDROID, StringUtils.defaultIfBlank(tokenStatsDo.getToken(), "NULL"), tokenStatsDo.getNotificationId()));
                            } else if (tokenDo.getDevice().equals("ios")) {
                                if (pushType == PushType.ALERT || pushType == PushType.BACKGROUND) {
                                    return notificationStatsService.createStats(requestBatchId, tokenDo.getToken(), tokenDo.getAccountId(), actionType, pushType.toString())
                                            .map(tokenStatsDo -> Triple.of(OSType.IOS, StringUtils.defaultIfBlank(tokenStatsDo.getToken(), "NULL"), tokenStatsDo.getNotificationId()));
                                } else if (pushType == PushType.VOIP) {
                                    return notificationStatsService.createStats(requestBatchId, tokenDo.getVoipToken(), tokenDo.getAccountId(), actionType, pushType.toString())
                                            .map(tokenStatsDo -> Triple.of(OSType.ANDROID, StringUtils.defaultIfBlank(tokenStatsDo.getToken(), "NULL"), tokenStatsDo.getNotificationId()));
                                }
                            }
                            return Mono.just(Triple.of(OSType.ANDROID, "NULL", "NULL"));
                        }).switchIfEmpty(notificationStatsService.createStats(requestBatchId, null, "", actionType, pushType.toString())
                                .map(tokenStatsDo -> Triple.of(OSType.ANDROID, StringUtils.defaultIfBlank(tokenStatsDo.getToken(), "NULL"), tokenStatsDo.getNotificationId()))));
    }
}
