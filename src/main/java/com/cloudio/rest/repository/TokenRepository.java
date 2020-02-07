package com.cloudio.rest.repository;

import com.cloudio.rest.entity.TokenDO;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface TokenRepository extends ReactiveMongoRepository<TokenDO, String> {

    Mono<TokenDO> findByAccountId(final String accountId);

    @Query("{'accountId' :{$in : ?0 }}")
    Flux<TokenDO> findByAccountIds(List<String> accountIds);
}
