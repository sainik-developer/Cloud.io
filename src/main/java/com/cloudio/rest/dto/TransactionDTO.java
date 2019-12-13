package com.cloudio.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {
    @NotBlank(message = "nonse can't be null")
    private String nonse;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String planId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String subscriptionId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String accountId;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime created;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updated;
}
