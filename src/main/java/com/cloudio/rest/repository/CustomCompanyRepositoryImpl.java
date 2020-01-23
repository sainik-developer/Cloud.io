package com.cloudio.rest.repository;

import com.cloudio.rest.entity.CompanyDO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomCompanyRepositoryImpl implements CustomCompanyRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<CompanyDO> deleteProfileUrlByAccountId(String companyId) {
        return reactiveMongoTemplate.findAndModify(Query.query(Criteria.where("companyId").is(companyId)),
                new Update().unset("companyAvatarUrl"), CompanyDO.class);
    }
}
