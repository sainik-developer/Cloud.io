package com.cloudio.rest.mapper;

import com.cloudio.rest.dto.CompanyDTO;
import com.cloudio.rest.entity.CompanyDO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CompanyMapper {

    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    CompanyDTO toDTO(CompanyDO companyDO);

    @Mapping(source = "adapterNumber",target="adapterNumber",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    CompanyDO fromDTO(CompanyDTO companyDTO);
}
