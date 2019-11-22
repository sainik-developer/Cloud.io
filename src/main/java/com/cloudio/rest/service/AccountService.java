package com.cloudio.rest.service;

import com.cloudio.rest.dto.AccountDTO;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.mapper.AccountMapper;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Mono<AccountDTO> createAccount(final String companyId, final String phoneNumber, final AccountType accountType, final String firstName, final String lastname) {
        return accountRepository.save(AccountDO.builder()
                .type(accountType)
                .firstName(firstName)
                .lastName(lastname)
                .accountId("CIO:ACC:" + UUID.randomUUID().toString())
                .companyId(companyId).phoneNumber(phoneNumber)
                .build())
                .doOnNext(accountDo -> log.info("Account is just created successfully for phone number {} and companyId {}", accountDo.getPhoneNumber(), accountDo.getCompanyId()))
                .map(AccountMapper.INSTANCE::toDTO);
    }

}
