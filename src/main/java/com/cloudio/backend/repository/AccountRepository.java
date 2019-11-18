package com.cloudio.backend.repository;

import com.cloudio.backend.model.Account;

import java.util.List;
import java.util.Optional;
public interface AccountRepository {

    boolean upsert(final Account account);

    Optional<List<Account>> findByPhoneNumber(final String phoneNumber);

    Optional<Account> findByPhoneNumberAndCompanyId(final String phoneNumber, final String companyId);


}
