package com.cloudio.rest.dto;

import com.cloudio.rest.pojo.CompanyStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String companyId;
    @NotBlank(message = "company name can't be null")
    private String name;
    private String companyAvatarUrl;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private CompanyStatus companyStatus;
    private String adapterNumber = "088-123 45 68";
}
