package com.cloudio.rest.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySetting {
    private RingType ringType;
    private List<String> ringOrderAccountIds;
    private Integer orderDelayInMin;
    private Boolean isVoiceMessage;
    private VoiceMessageSetting voiceMessageSetting;
}
