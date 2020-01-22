package com.cloudio.rest.service;

import com.cloudio.rest.dto.AccountDTO;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.mapper.AccountMapper;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.BrainTreeDetail;
import com.cloudio.rest.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AccountService {

    @Value("${payment.bt.planId}")
    private String planId;

    private final AccountRepository accountRepository;

    public Mono<AccountDTO> createAccount(final String companyId, final String phoneNumber, final AccountType accountType, final String firstName, final String lastname) {
        return accountRepository.save(createDO(companyId, phoneNumber, accountType, firstName, lastname))
                .doOnNext(accountDo -> log.info("Account is just created successfully for phone number {} and companyId {}", accountDo.getPhoneNumber(), accountDo.getCompanyId()))
                .map(AccountMapper.INSTANCE::toDTO);
    }

    private AccountDO createDO(final String companyId, final String phoneNumber, final AccountType accountType, final String firstName, final String lastname) {
        return AccountDO.builder()
                .type(accountType).firstName(firstName)
                .detail(accountType == AccountType.ADMIN ? BrainTreeDetail.builder().planId(planId).build() : null)
                .lastName(lastname).status(AccountStatus.ACTIVE)
                .accountId("CIO:ACC:" + UUID.randomUUID().toString())
                .companyId(companyId).phoneNumber(phoneNumber)
                .build();
    }
}
