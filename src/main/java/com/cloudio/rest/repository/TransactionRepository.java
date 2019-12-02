package com.cloudio.rest.repository;

import com.cloudio.rest.entity.TransactionDO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<TransactionDO, String> {
}
