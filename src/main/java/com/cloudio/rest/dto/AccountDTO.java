package com.cloudio.rest.dto;

import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.AskfastDetail;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

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
    @NotEmpty
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String profileUrl;
    @JsonIgnore
    private String firebaseAuthToken;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final AccountStatus status = AccountStatus.ACTIVE;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private AccountType type;
    @JsonIgnore
    private AskfastDetail askfastDetail;
}
