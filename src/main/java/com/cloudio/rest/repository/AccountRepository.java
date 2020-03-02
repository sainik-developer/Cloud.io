package com.cloudio.rest.repository;

import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.pojo.AccountState;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface AccountRepository extends ReactiveMongoRepository<AccountDO, String>, CustomAccountRepository {
    Flux<AccountDO> findByPhoneNumber(final String phoneNumber);

    Mono<AccountDO> findByPhoneNumberAndCompanyId(final String phoneNumber, final String companyId);

    Mono<AccountDO> findByAccountIdAndStatus(final String accountId, final AccountStatus status);

    Mono<AccountDO> findByAccountIdAndStatusAndType(final String accountId, final AccountStatus status, final AccountType type);

    Mono<AccountDO> findByAccountIdAndCompanyIdAndTypeAndStatus(final String accountId, final String companyId, final AccountType accountType, final AccountStatus status);

    Mono<AccountDO> findByCompanyIdAndType(final String companyId, final AccountType type);

    Mono<AccountDO> findByAccountId(final String accountId);

    @Query("{'accountId' :{$in : ?0 },'status' : ?1}")
    Flux<AccountDO> findByAccountIdsAndStatus(final List<String> accountIds, final AccountStatus status);

    Flux<AccountDO> findByCompanyIdAndStatus(final String companyId, final AccountStatus status);

    Flux<AccountDO> findByCompanyIdAndStatusAndState(final String companyId, final AccountStatus status, final AccountState state);

    Mono<AccountDO> findByAccountIdAndStatusAndState(final String accountId, final AccountStatus status, final AccountState state);
}
