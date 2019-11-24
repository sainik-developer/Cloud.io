package com.cloudio.rest.service;

import com.cloudio.rest.entity.CompanyDO;
import com.cloudio.rest.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;


    public Mono<Boolean> isCompanyNameUnique(final String companyName) {
        return companyRepository.findByName(companyName)
                .map(companyDO -> Boolean.FALSE)
                .switchIfEmpty(Mono.just(Boolean.TRUE));
    }
}
