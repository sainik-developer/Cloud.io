package com.cloudio.backend.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;
public interface FirebaseTokenRepository {

    Optional<Boolean> removeByAccountId(final String accountId);
}
