package com.cloudio.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TempAuthToken {

    private String phoneNumber;
    private String code;
    private LocalDateTime createTime;
}
