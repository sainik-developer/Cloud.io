package com.cloudio.rest.dto;

import com.cloudio.rest.validator.NotificationValidator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationSendRequestDTO {
    @NotBlank(groups = {NotificationValidator.AccountIdValidate.class},message = "AccountId can't be blank!")
    @NotEmpty(groups = {NotificationValidator.AccountIdValidate.class},message = "AccountId can't be empty!")
    private String accountId;
    @NotBlank(groups = {NotificationValidator.CompanyIdValidate.class},message = "CompanyId can't be blank!")
    @NotEmpty(groups = {NotificationValidator.CompanyIdValidate.class},message = "CompanyId can't be empty!")
    private String companyId;
    @NotBlank(groups = {NotificationValidator.GroupIdValidate.class},message = "GroupId can't be blank!")
    @NotEmpty(groups = {NotificationValidator.GroupIdValidate.class},message = "GroupId can't be empty!")
    private String groupId;
    @NotEmpty(message = "data can't be empty")
    private Map<String, String> data;
}
