package com.cloudio.rest.mapper;

import com.cloudio.rest.dto.GroupDTO;
import com.cloudio.rest.entity.GroupDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GroupMapper {

    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    GroupDTO toDTO(GroupDO groupDO);

    GroupDO fromDTO(GroupDTO groupDTO);
}
