package com.cloudio.rest.repository;

import com.cloudio.rest.entity.TokenStatsDO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenStatsRepository extends ReactiveMongoRepository<TokenStatsDO, String> {

    TokenStatsDO findByNotificationId(String apnsId);

    @Query("{}")
    List<TokenStatsDO> findByDateTime(final Pageable pageable);
}
