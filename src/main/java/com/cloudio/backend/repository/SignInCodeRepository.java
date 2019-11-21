package com.cloudio.backend.repository;

import com.cloudio.backend.entity.SignInDetailDO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SignInCodeRepository extends ReactiveMongoRepository<SignInDetailDO, String> {

    Mono<SignInDetailDO> findByPhoneNumber(final String phoneNumber);

}
