package com.cloudio.rest.dto;

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
public class BrainTreeWebHookRequestDTO {
    @JsonProperty("bt_signature")
    private String btSignature;
    @JsonProperty("bt_payload")
    private String btPayload;
}
