package com.cloudio.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignInDetails {

    public static final String COLLECTION_NAME = "signincodes";
    private String smsCode;
    private String phoneNumber;
    private LocalDateTime updated;
    private int retry;

    public void increaseRetries(int maxTry) {
        retry = retry >= maxTry ? 1 : retry + 1;
        this.updated = LocalDateTime.now();
    }
}
