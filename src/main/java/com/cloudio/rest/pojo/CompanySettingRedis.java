package com.cloudio.rest.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySettingRedis {
    @Id
    private String id;
    @Indexed
    private String adapterNumber;
    private RingType ringType;
    private List<String> ringOrderAccountIds;
    private Integer orderDelayInMin;
    private Boolean isVoiceMessage;
    private VoiceMessageSetting voiceMessageSetting;
    @TimeToLive
    private Long expiration;
}
