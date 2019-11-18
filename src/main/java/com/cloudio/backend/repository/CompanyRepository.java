package com.cloudio.backend.repository;

import com.cloudio.backend.model.Company;

import java.util.Optional;
public interface CompanyRepository {

    Optional<Company> findByCompanyId(final String companyId);

    Optional<Company> saveCompany(final Company company);


}
