package com.cloudio.rest.mapper;

import com.cloudio.rest.dto.TokenDTO;
import com.cloudio.rest.entity.TokenDO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FirebaseTokenMapper {

    FirebaseTokenMapper INSTANCE = Mappers.getMapper(FirebaseTokenMapper.class);

    TokenDO fromDto(TokenDTO dto);

    void update(@MappingTarget TokenDO aDo, TokenDTO dto);
}

