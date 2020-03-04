package com.cloudio.rest.mapper;

import com.cloudio.rest.dto.CompanySettingDTO;
import com.cloudio.rest.pojo.CompanySetting;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CompanySettingMapper {

    CompanySettingMapper INSTANCE = Mappers.getMapper(CompanySettingMapper.class);

    CompanySetting fromDTO(CompanySettingDTO companySettingDTO);

    CompanySettingDTO toDTO(CompanySetting companySetting);
}
