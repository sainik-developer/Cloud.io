package com.cloudio.rest.service;

import com.cloudio.rest.entity.GroupDO;
import com.cloudio.rest.entity.TokenDO;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.GroupType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import com.cloudio.rest.repository.TokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
@Service
public class NotificationService {
    private final TokenRepository tokenRepository;
    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;
    private final FirebaseService firebaseService;

    public Mono<Boolean> sendNotification(final String token, final Map<String, Object> data) {
        if (flag)
            return Mono.just(true);
        return Mono.just(false);
    }

    public Mono<Boolean> sendNotificationToGroup(final GroupDO groupDO, final Map<String, Object> data) {
        Mono.just(groupDO)
                .filter(groupDo -> groupDo.getGroupType() == GroupType.DEFAULT)
                .flatMap(groupDo -> companyRepository.findByCompanyId(groupDO.getCompanyId()))
                .flatMapMany(companyDO -> accountRepository.findByCompanyIdAndStatus(companyDO.getCompanyId(), AccountStatus.ACTIVE))
                .flatMap(accountDO -> tokenRepository.findByAccountId(accountDO.getAccountId()))
                .groupBy(TokenDO::getDevice)
                .flatMap(tokenDOGroupedFlux -> tokenDOGroupedFlux.collectList()
                        .flatMap(tokenDos -> tokenDOGroupedFlux.key().equals("ios")?apnsService.send(tokenDos,data):firebaseService.send(tokenDos,data)))
                .switchIfEmpty(Mono.error(new RuntimeException("Only default group is supported now!")));

    }

}
