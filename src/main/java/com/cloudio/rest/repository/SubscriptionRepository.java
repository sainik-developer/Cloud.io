package com.cloudio.rest.repository;

import com.cloudio.rest.entity.SubscriptionDO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SubscriptionRepository extends ReactiveMongoRepository<SubscriptionDO, String> {
    Mono<SubscriptionDO> findByAccountId(final String accountId);
}
