package com.cloudio.rest.mapper;

import com.cloudio.rest.dto.CompanyDTO;
import com.cloudio.rest.entity.CompanyDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = CompanySettingMapper.class)
public interface CompanyMapper {

    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    @Mapping(source = "adapterNumber", target = "adapterNumber", defaultValue = "NO VALID ADAPTER")
    CompanyDTO toDTO(CompanyDO companyDO);

    CompanyDO fromDTO(CompanyDTO companyDTO);
}