package com.cloudio.rest.repository;

import com.cloudio.rest.entity.CompanyDO;
import com.cloudio.rest.pojo.CompanyStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CompanyRepository extends ReactiveMongoRepository<CompanyDO, String>, CustomCompanyRepository {
    Mono<CompanyDO> findByCompanyId(final String companyId);

    Mono<CompanyDO> findByName(final String name);

    Mono<CompanyDO> findByAdapterNumber(final String adapterNumber);

    Mono<CompanyDO> findByCompanyIdAndCompanyStatus(final String companyId, final CompanyStatus companyStatus);
}
