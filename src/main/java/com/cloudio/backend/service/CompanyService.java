package com.cloudio.backend.service;

import com.cloudio.backend.repository.CompanyRepository;
import com.cloudio.backend.model.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public Company createCompany(Company company){
        company.setCompanyId(UUID.randomUUID().toString());
        Optional<Company> response = companyRepository.saveCompany(company);
        return response.orElse(null);

    }
}
