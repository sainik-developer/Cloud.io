package com.cloudio.rest.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceMessageSetting {
    @NotEmpty
    private String content;
    @Pattern(regexp = "ENGLISH|DUTCH")
    private String lang;
    @Pattern(regexp = "MALE|FEMALE")
    private String voiceType;
    @Max(value = 30)
    @Min(value = 5)
    private Integer playAfterInSec;
    private Boolean afterLastColleagueInList;
}
