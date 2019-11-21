package com.cloudio.backend.repository;

import com.cloudio.backend.model.FirebaseToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FirebaseTokenRepository extends ReactiveMongoRepository<FirebaseToken, String> {

    Optional<Long> deleteByAccountId(final String accountId);
}
