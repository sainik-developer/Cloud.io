package com.cloudio.rest.dto;

import com.cloudio.rest.pojo.RingType;
import com.cloudio.rest.pojo.VoiceMessageSetting;
import com.cloudio.rest.validator.ValidCompanySetting;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ValidCompanySetting
public class CompanySettingDTO {
    @NotNull
    private RingType ringType;
    private List<String> ringOrderAccountIds;
    private Integer orderDelayInSec = 5;
    private Boolean isVoiceMessage;
    @Valid
    @NotNull
    private VoiceMessageSetting voiceMessageSetting;
}
