package com.cloudio.rest.mapper;

import com.cloudio.rest.dto.TransactionDTO;
import com.cloudio.rest.entity.TransactionDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    TransactionDTO toDTO(TransactionDO transactionDO);

    TransactionDO fromDTO(TransactionDTO transactionDTO);
}
