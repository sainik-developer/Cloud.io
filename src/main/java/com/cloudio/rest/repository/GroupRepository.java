package com.cloudio.rest.repository;

import com.cloudio.rest.entity.GroupDO;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface GroupRepository extends ReactiveMongoRepository<GroupDO, String> {
    Flux<GroupDO> findByCompanyId(final String accountId);
}
