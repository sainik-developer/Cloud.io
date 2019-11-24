package com.cloudio.rest.dto;

import com.cloudio.rest.validator.ValidPhoneNumber;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class InviteAccountDTO {
    @ValidPhoneNumber
    private String phoneNumber;
    @NotEmpty(message = "invited account should have valid first name")
    private String firstName;
    @NotEmpty(message = "invited account should have valid last name")
    private String lastName;
}
