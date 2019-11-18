package com.cloudio.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Company {

    public static final String COLLECTION_NAME = "companies";
    
    private ObjectId id;
    private String companyId;
    private String name;
    private String companyAvatarUrl;
    private CompanyStatus companyStatus;

}
