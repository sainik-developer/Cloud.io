package com.cloudio.rest.mapper;

import com.cloudio.rest.dto.AccountDTO;
import com.cloudio.rest.dto.GroupDTO;
import com.cloudio.rest.entity.GroupDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GroupMapper {

    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    GroupDTO toDTO(GroupDO groupDO);

    GroupDO fromDTO(GroupDTO groupDTO);

    default AccountDTO toDto(final String accountId) {
        return AccountDTO.builder().accountId(accountId).build();
    }

    default String fromAccountDto(final AccountDTO accountDTO) {
        return accountDTO.getAccountId();
    }
}
