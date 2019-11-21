package com.cloudio.backend.service;

import com.cloudio.backend.dto.AccountDTO;
import com.cloudio.backend.entity.AccountDO;
import com.cloudio.backend.mapper.AccountMapper;
import com.cloudio.backend.pojo.AccountType;
import com.cloudio.backend.repository.AccountRepository;
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

    public Mono<AccountDTO> createAccount(final String companyId, final String phoneNumber, final AccountType accountType) {
        return accountRepository.save(AccountDO.builder()
                .type(accountType)
                .accountId("CIO:ACC:" + UUID.randomUUID().toString())
                .companyId(companyId).phoneNumber(phoneNumber)
                .build())
                .doOnNext(accountDo -> log.info("Account is just created successfully for phone number {} and companyId {}", accountDo.getPhoneNumber(), accountDo.getCompanyId()))
                .map(AccountMapper.INSTANCE::toDTO);
    }

}
