package com.cloudio.rest.service;

import com.cloudio.rest.entity.GroupDO;
import com.cloudio.rest.entity.TokenDO;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.GroupType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import com.cloudio.rest.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
@Service
public class NotificationService {
    private final TokenRepository tokenRepository;
    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;
    private final FirebaseService firebaseService;

    public Mono<Boolean> sendNotificationToAccount(final TokenDO tokenDO, final Map<String, String> data) {
        return Mono.just(tokenDO.getDevice().equals("ios") ? apnsService.send() : firebaseService.sendNotificationToAccount(tokenDO.getToken(), data))
                .switchIfEmpty(new RuntimeException());
    }

    public Mono<Boolean> sendNotificationToGroup(final GroupDO groupDO, final Map<String, String> data) {
        return Mono.just(groupDO)
                .filter(groupDo -> groupDo.getGroupType() == GroupType.DEFAULT)
                .flatMap(groupDo -> companyRepository.findByCompanyId(groupDO.getCompanyId()))
                .flatMapMany(companyDO -> accountRepository.findByCompanyIdAndStatus(companyDO.getCompanyId(), AccountStatus.ACTIVE))
                .flatMap(accountDO -> tokenRepository.findByAccountId(accountDO.getAccountId()))
                .groupBy(TokenDO::getDevice)
                .flatMap(tokenDOGroupedFlux -> tokenDOGroupedFlux.collectList()
                        .flatMap(tokenDos -> tokenDOGroupedFlux.key().equals("ios") ? apnsService.send(tokenDos, data) : firebaseService.send(tokenDos, data)))
                .switchIfEmpty(Mono.error(new RuntimeException("Only default group is supported now!")));

    }

}
