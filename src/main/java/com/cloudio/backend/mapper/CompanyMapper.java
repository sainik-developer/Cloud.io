package com.cloudio.backend.mapper;

import com.cloudio.backend.dto.CompanyDTO;
import com.cloudio.backend.entity.CompanyDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CompanyMapper {

    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    CompanyDTO toDTO(CompanyDO companyDO);

    CompanyDO fromDTO(CompanyDTO companyDTO);
}
