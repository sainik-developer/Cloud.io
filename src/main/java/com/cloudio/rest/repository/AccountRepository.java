package com.cloudio.rest.repository;

import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends ReactiveMongoRepository<AccountDO, String> {
    Flux<AccountDO> findByPhoneNumber(final String phoneNumber);

    Mono<AccountDO> findByPhoneNumberAndCompanyId(final String phoneNumber, final String companyId);

    Mono<AccountDO> findByAccountIdAndStatus(final String accountId, final AccountStatus status);

    Mono<AccountDO> findByAccountIdAndCompanyIdAndType(final String accountId, final String companyId, final AccountType accountType);
}
