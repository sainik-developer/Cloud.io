package com.cloudio.rest.repository;

import com.cloudio.rest.entity.CompanyDO;
import reactor.core.publisher.Mono;

public interface CustomCompanyRepository {
    Mono<CompanyDO> deleteProfileUrlByAccountId(final String companyId);
}
