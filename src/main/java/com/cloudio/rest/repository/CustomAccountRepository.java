package com.cloudio.rest.repository;

import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.pojo.AccountStatus;
import reactor.core.publisher.Mono;

public interface CustomAccountRepository {
    Mono<AccountDO> deleteProfileUrlByAccountId(final String accountId, final AccountStatus accountStatus);
}
