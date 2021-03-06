package com.cloudio.rest.service;

import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.entity.TokenDO;
import com.cloudio.rest.pojo.AccountState;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.BrainTreeDetail;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TokenRepository tokenRepository;

    @Value("${payment.bt.planId}")
    private String planId;

    public Mono<AccountDO> createAccount(final String companyId, final String phoneNumber, final AccountType accountType, final String firstName, final String lastname) {
        return accountRepository.findByPhoneNumberAndCompanyId(phoneNumber, companyId)
                .doOnNext(accountDo -> log.info("Account is found with phone Number {} for company {}", accountDo.getPhoneNumber(), companyId))
                .switchIfEmpty(accountRepository.save(createDO(companyId, phoneNumber, accountType, firstName, lastname))
                        .doOnNext(accountDo -> log.info("Account is just created successfully for phone number {} and companyId {}", accountDo.getPhoneNumber(), accountDo.getCompanyId())));
    }

    private AccountDO createDO(final String companyId, final String phoneNumber, final AccountType accountType, final String firstName, final String lastname) {
        return AccountDO.builder()
                .type(accountType).firstName(firstName)
                .detail(accountType == AccountType.ADMIN ? BrainTreeDetail.builder().planId(planId).build() : null)
                .lastName(lastname).status(AccountStatus.ACTIVE)
                .state(AccountState.ONLINE)
                .accountId("CIO:ACC:" + UUID.randomUUID().toString())
                .companyId(companyId).phoneNumber(phoneNumber)
                .build();
    }

    public Flux<String> getTokenRegisteredAccount(final String companyId) {
        return accountRepository.findByCompanyIdAndStatusAndState(companyId, AccountStatus.ACTIVE, AccountState.ONLINE)
                .map(AccountDO::getAccountId)
                .collectList()
                .flatMapMany(tokenRepository::findByAccountIds)
                .map(TokenDO::getAccountId)
                .switchIfEmpty(Flux.empty());
    }

    public Flux<String> getTokenRegisteredOnlineAndActiveAccount(final List<String> accountIDs) {
        return Flux.fromIterable(accountIDs)
                .flatMap(accountId -> accountRepository.findByAccountIdAndStatusAndState(accountId, AccountStatus.ACTIVE, AccountState.ONLINE))
                .map(AccountDO::getAccountId)
                .flatMap(tokenRepository::findByAccountId)
                .map(TokenDO::getAccountId)
                .switchIfEmpty(Flux.empty());// give proper exception and message
    }
}
