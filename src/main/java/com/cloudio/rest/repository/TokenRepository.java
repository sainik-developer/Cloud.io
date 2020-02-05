package com.cloudio.rest.repository;

import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.entity.TokenDO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TokenRepository extends ReactiveMongoRepository<TokenDO, String> {

    Mono<TokenDO> findByAccountId(final String accountId);
}
