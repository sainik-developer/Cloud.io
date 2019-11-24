package com.cloudio.rest.dto;

import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String accountId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String companyId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String jobTitle;
    @JsonIgnore
    private String firebaseAuthToken;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final AccountStatus status = AccountStatus.ACTIVE;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private AccountType type;
}
