package com.cloudio.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {
    @JsonProperty(value = "Authorization")
    private String authorization;
/*
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<GroupDTO> groups;*/

    private String accountId;
    private String refreshToken;

    //@JsonProperty(access = JsonProperty.Access.READ_ONLY)
    //    private List<GroupDTO> groups;
}
