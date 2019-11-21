package com.cloudio.backend.dto;

import com.cloudio.backend.validator.ValidPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InviteAccountDTO {
    @ValidPhoneNumber
    private String phoneNumber;
    @NotEmpty(message = "invited account should have valid first name")
    private String firstName;
    @NotEmpty(message = "invited account should have valid last name")
    private String lastName;
}
