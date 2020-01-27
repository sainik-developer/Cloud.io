package com.cloudio.rest.service;

import com.cloudio.rest.dto.AccountDTO;
import com.cloudio.rest.entity.GroupDO;
import com.cloudio.rest.pojo.AccountState;
import com.cloudio.rest.pojo.GroupState;
import com.cloudio.rest.pojo.GroupType;
import com.cloudio.rest.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.cloudio.rest.pojo.GroupState.OFFLINE;
import static com.cloudio.rest.pojo.GroupState.ONLINE;

@Log4j2
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    public Mono<GroupDO> createDefaultGroup(final String companyId) {
        return groupRepository.save(GroupDO.builder().companyId(companyId).groupId("CIO:GR:"+UUID.randomUUID().toString()).groupType(GroupType.DEFAULT).build());
    }

    public GroupState getGroupStatus(List<AccountDTO> accountDTOS){
        return accountDTOS.stream().map(AccountDTO::getState).anyMatch(accountState -> accountState== AccountState.ONLINE) ?  ONLINE : OFFLINE;
    }
}