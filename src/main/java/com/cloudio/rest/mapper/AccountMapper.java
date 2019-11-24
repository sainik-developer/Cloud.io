package com.cloudio.rest.mapper;

import com.cloudio.rest.dto.AccountDTO;
import com.cloudio.rest.entity.AccountDO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    AccountDTO toDTO(final AccountDO accountDO);

    AccountDO fromDTO(final AccountDTO accountDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    void update(@MappingTarget AccountDO toAccountDTO, AccountDTO fromAccountDTO);
}
