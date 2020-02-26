package com.cloudio.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TwilioCallRequestDTO {
    @JsonProperty(value = "msg")
    private String msg;
    @JsonProperty(value = "Digits")
    private String digits;
    @JsonProperty(value = "Called")
    private String called;
    @JsonProperty(value = "ToState")
    private String toState;
    @JsonProperty(value = "CallerCountry")
    private String callerCountry;
    @JsonProperty(value = "Direction")
    private String direction;
    @JsonProperty(value = "CallerState")
    private String callerState;
    @JsonProperty(value = "ToZip")
    private String toZip;
    @JsonProperty(value = "CallSid")
    private String callSid;
    @JsonProperty(value = "To")
    private String to;
    @JsonProperty(value = "CallerZip")
    private String callerZip;
    @JsonProperty(value = "ToCountry")
    private String toCountry;
    @JsonProperty(value = "ApiVersion")
    private String apiVersion;
    @JsonProperty(value = "CalledZip")
    private String calledZip;
    @JsonProperty(value = "CalledCity")
    private String calledCity;
    @JsonProperty(value = "CallStatus")
    private String callStatus;
    @JsonProperty(value = "From")
    private String from;
    @JsonProperty(value = "AccountSid")
    private String accountSid;
    @JsonProperty(value = "calledCountry")
    private String calledCountry;
    @JsonProperty(value = "CallerCity")
    private String callerCity;
    @JsonProperty(value = "ApplicationSid")
    private String applicationSid;
    @JsonProperty(value = "Caller")
    private String caller;
    @JsonProperty(value = "FromCountry")
    private String fromCountry;
    @JsonProperty(value = "ToCity")
    private String toCity;
    @JsonProperty(value = "FromCity")
    private String fromCity;
    @JsonProperty(value = "CalledState")
    private String calledState;
    @JsonProperty(value = "FromZip")
    private String fromZip;
    @JsonProperty(value = "FromState")
    private String fromState;

    @JsonIgnore
    private String fromPhoneNumber;
    @JsonIgnore
    private String fromNameCloudIO;
}
