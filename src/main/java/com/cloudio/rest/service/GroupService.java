package com.cloudio.rest.service;

import com.cloudio.rest.entity.GroupDO;
import com.cloudio.rest.pojo.GroupType;
import com.cloudio.rest.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    public Mono<GroupDO> createDefaultGroup(final String companyId) {
        return groupRepository.save(GroupDO.builder().companyId(companyId).groupId("CIO:GR:"+UUID.randomUUID().toString()).groupType(GroupType.DEFAULT).build());
    }

}