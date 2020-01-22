package com.cloudio.rest.entity;


import com.cloudio.rest.pojo.GroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Builder
@Document(value = "groups")
@NoArgsConstructor
@AllArgsConstructor
public class GroupDO {
    @Id
    private String id;

    private String groupId;
    private String companyId;
    private GroupType groupType;
}
