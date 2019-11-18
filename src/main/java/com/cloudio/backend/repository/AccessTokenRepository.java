package com.cloudio.backend.repository;

import com.cloudio.backend.model.AccessToken;

import java.util.Optional;
public interface AccessTokenRepository {

    Optional<AccessToken> findByToken(final String token);

    boolean upsert(final AccessToken accessToken);

    Optional<Boolean> removeByToken(final String token);
}
