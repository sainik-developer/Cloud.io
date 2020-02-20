package com.cloudio.rest.service;

import com.cloudio.rest.dto.CompanyDTO;
import com.cloudio.rest.pojo.CompanyStatus;
import com.cloudio.rest.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final AdapterService adapterService;

    public Mono<Boolean> isCompanyNameUnique(final String companyName) {
        return companyRepository.findByName(companyName)
                .map(companyDO -> Boolean.FALSE)
                .switchIfEmpty(Mono.just(Boolean.TRUE));
    }

    public Mono<CompanyDTO> createCompany(final CompanyDTO companyDTO) {
        return adapterService.getAvailableAdapter()
                .map(s -> {
                    companyDTO.setCompanyId("CIO:COM:" + UUID.randomUUID().toString());
                    companyDTO.setCompanyStatus(CompanyStatus.NOT_VERIFIED);
                    companyDTO.setAdapterNumber(s);
                    return companyDTO;
                });
    }
}
