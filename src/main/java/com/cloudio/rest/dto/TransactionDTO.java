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
    private String planId;
    private String subscriptionId;
    @NotBlank(message = "company id can't be null")
    private String companyId;
    private String accountId;
    private LocalDateTime created;
    private LocalDateTime updated;
}
