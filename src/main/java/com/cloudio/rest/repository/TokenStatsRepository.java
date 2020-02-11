package com.cloudio.rest.repository;

import com.cloudio.rest.entity.TokenStatsDO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TokenStatsRepository extends ReactiveMongoRepository<TokenStatsDO, String> {

    Mono<TokenStatsDO> findByNotificationId(String apnsId);

    @Query("{}")
    Flux<TokenStatsDO> findByDateTime(final Pageable pageable);
}
