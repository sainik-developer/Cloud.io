package com.cloudio.backend.mapper;

import com.cloudio.backend.dto.AccountDTO;
import com.cloudio.backend.entity.AccountDO;
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
