package com.cloudio.rest.repository;

import com.cloudio.rest.entity.CompanyDO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CompanyRepository extends ReactiveMongoRepository<CompanyDO, String> {

    Mono<CompanyDO> findByCompanyId(final String companyId);


}
