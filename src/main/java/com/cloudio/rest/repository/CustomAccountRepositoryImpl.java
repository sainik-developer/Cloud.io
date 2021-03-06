package com.cloudio.rest.repository;

import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.pojo.AccountState;
import com.cloudio.rest.pojo.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomAccountRepositoryImpl implements CustomAccountRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<AccountDO> deleteProfileUrlByAccountId(String accountId, final AccountStatus accountStatus) {
        return reactiveMongoTemplate.findAndModify(Query.query(Criteria.where("accountId").is(accountId).andOperator(Criteria.where("status").is(accountStatus.toString()))),
                new Update().unset("profileUrl"), AccountDO.class);
    }

    @Override
    public Mono<AccountDO> updateByAccountId(final String accountId, final AccountStatus status, final AccountState state) {
        return reactiveMongoTemplate.findAndModify(Query.query(Criteria.where("accountId").is(accountId).andOperator(Criteria.where("status").is(status.toString()))),
                new Update().set("state", state), AccountDO.class)
                .flatMap(accountDO -> reactiveMongoTemplate.findOne(Query.query(Criteria.where("accountId").is(accountId)), AccountDO.class));
    }
}
