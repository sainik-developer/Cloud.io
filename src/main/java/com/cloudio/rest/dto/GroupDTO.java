package com.cloudio.rest.dto;

import com.cloudio.rest.entity.GroupDO;
import com.cloudio.rest.pojo.GroupType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {
    private String groupId;
    @JsonIgnore
    private String companyId;

    private GroupType groupType;
}
