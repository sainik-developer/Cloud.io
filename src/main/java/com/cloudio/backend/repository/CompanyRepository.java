package com.cloudio.backend.repository;

import com.cloudio.backend.entity.CompanyDO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface CompanyRepository extends ReactiveMongoRepository<CompanyDO, String> {

    Mono<CompanyDO> findByCompanyId(final String companyId);


}
