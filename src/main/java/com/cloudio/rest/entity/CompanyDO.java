package com.cloudio.rest.entity;

import com.cloudio.rest.pojo.CompanySetting;
import com.cloudio.rest.pojo.CompanyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document("companies")
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDO {
    @Id
    private String id;

    private String companyId;
    private String name;
    private String companyAvatarUrl;
    private CompanyStatus companyStatus;
    private String adapterNumber;
    @CreatedDate
    private LocalDateTime created;
    @LastModifiedDate
    private LocalDateTime updated;
    private CompanySetting companySetting;
}
