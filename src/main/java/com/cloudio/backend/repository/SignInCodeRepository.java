package com.cloudio.backend.repository;

import com.cloudio.backend.model.SignInDetails;

import java.util.Optional;
public interface SignInCodeRepository {

    Optional<SignInDetails> findByPhoneNumber(final String phoneNumber);

    Optional<SignInDetails> upsert(final SignInDetails signInDetails);
}
