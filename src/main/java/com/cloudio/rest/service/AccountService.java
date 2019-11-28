package com.cloudio.rest.service;

import com.cloudio.rest.dto.AccountDTO;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.entity.CompanyDO;
import com.cloudio.rest.exception.SuspiciousStateException;
import com.cloudio.rest.mapper.AccountMapper;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.AskfastDetail;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AskFastService askFastService;
    private final CompanyRepository companyRepository;

    public Mono<AccountDTO> createAccount(final String companyId, final String phoneNumber, final AccountType accountType, final String firstName, final String lastname) {
        return Mono.zip(accountRepository.save(createDO(companyId, phoneNumber, accountType, firstName, lastname))
                        .doOnNext(accountDO -> log.info("got1")).doOnError(throwable -> log.error(throwable.getMessage())),
                companyRepository.findByCompanyId(companyId).doOnNext(accountDO -> log.info("got2")).doOnError(throwable -> log.error(throwable.getMessage())))
                .doOnError(throwable -> log.error("error"))
                .doOnNext(accountDOCompanyDOTuple2 -> log.info("Account with details {} is saved in db and corresponding company details is {}", accountDOCompanyDOTuple2.getT1(), accountDOCompanyDOTuple2.getT2()))
                .flatMap(accountDOCompanyDOTuple -> createAskfastAccount(accountDOCompanyDOTuple).map(askfastDetail -> {
                    accountDOCompanyDOTuple.getT1().setAskfastDetail(askfastDetail);
                    return accountDOCompanyDOTuple.getT1();
                }))
                .flatMap(accountRepository::save)
                .doOnNext(accountDo -> log.info("Account is just created successfully for phone number {} and companyId {}", accountDo.getPhoneNumber(), accountDo.getCompanyId()))
                .map(AccountMapper.INSTANCE::toDTO);
    }

    private Mono<AskfastDetail> createAskfastAccount(final Tuple2<AccountDO, CompanyDO> tuple2) {
        switch (tuple2.getT1().getType()) {
            case ADMIN:
                return askFastService.createAdminAccount(tuple2.getT1(), tuple2.getT2().getName());
            case MEMBER:
                return accountRepository.findByCompanyIdAndType(tuple2.getT1().getCompanyId(), AccountType.ADMIN)
                        .flatMap(adminAccountDo -> askFastService.createMemberAccount(tuple2.getT1(), adminAccountDo, tuple2.getT2().getName()));
            default:
                log.error("Account with wrong type {}", tuple2.getT1());
                return Mono.error(new SuspiciousStateException());
        }
    }

    private AccountDO createDO(final String companyId, final String phoneNumber, final AccountType accountType, final String firstName, final String lastname) {
        return AccountDO.builder()
                .type(accountType).firstName(firstName)
                .lastName(lastname).status(AccountStatus.ACTIVE)
                .accountId("CIO:ACC:" + UUID.randomUUID().toString())
                .companyId(companyId).phoneNumber(phoneNumber)
                .build();
    }
}
