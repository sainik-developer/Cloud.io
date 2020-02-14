package com.cloudio.rest.dto;

import com.cloudio.rest.validator.ValidationMarker;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationSendRequestDTO {
    @NotBlank(groups = {ValidationMarker.AccountIDMandatoryMarker.class},message = "AccountId can't be empty or blank!")
    private String accountId;
    @NotBlank(groups = {ValidationMarker.CompanyIDMandatoryMarker.class},message = "CompanyId can't be empty or blank!")
    private String companyId;
    @NotBlank(groups = {ValidationMarker.GroupIDMandatoryMarker.class},message = "GroupId can't be empty or blank!")
    private String groupId;
    @NotBlank(message = "data can't be empty or blank!")
    private Map<String, String> data;
}
