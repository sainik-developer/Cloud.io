package com.cloudio.rest.mapper;

import com.cloudio.rest.dto.AccountDTO;
import com.cloudio.rest.entity.AccountDO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    AccountDTO toDTO(final AccountDO accountDO);

    AccountDO fromDTO(final AccountDTO accountDTO);

    void update(@MappingTarget AccountDO toAccountDTO, AccountDTO fromAccountDTO);
}
