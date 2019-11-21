package com.cloudio.backend.repository;

import com.cloudio.backend.entity.AccessTokenDO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AccessTokenRepository extends ReactiveMongoRepository<AccessTokenDO, String> {

    Mono<AccessTokenDO> findByAccountId(final String accountId);

    Mono<AccessTokenDO> findByToken(final String token);

    Mono<Long> deleteByToken(final String token);
}
