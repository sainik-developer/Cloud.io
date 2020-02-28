package com.cloudio.rest.service;

import com.cloudio.rest.entity.TokenStatsDO;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.TokenStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class NotificationStatsService {
    private final AccountRepository accountRepository;
    private final TokenStatsRepository tokenStatsRepository;

    public Mono<TokenStatsDO> createStats(final String batchId, final String token, final String accountId, final String actionType, final String pushType) {
        return accountRepository.findByAccountId(accountId)
                .map(accountDO -> TokenStatsDO.builder().status(token == null ? "NOT REGISTERED (NO TOKEN)" : "INITIATED").batchId(batchId)
                        .token(token)
                        .accountName(accountDO.getFirstName() + accountDO.getLastName())
                        .phoneNumber(accountDO.getPhoneNumber())
                        .notificationId(UUID.randomUUID().toString())
                        .actionType(actionType)
                        .pushType(pushType)
                        .build())
                .flatMap(tokenStatsRepository::save)
                .switchIfEmpty(Mono.just(TokenStatsDO.builder().status(token == null ? "NOT REGISTERED (NO TOKEN)" : "INITIATED").batchId(batchId)
                        .token(token)
                        .notificationId(UUID.randomUUID().toString())
                        .actionType(actionType)
                        .pushType(pushType)
                        .build()).flatMap(tokenStatsRepository::save));
    }
}
