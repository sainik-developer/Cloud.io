package com.cloudio.rest.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document("signincodes")
@AllArgsConstructor
@NoArgsConstructor
public class SignInDetailDO {
    @Id
    private String id;

    private String smsCode;
    private String phoneNumber;
    @LastModifiedDate
    private LocalDateTime updated;
    private int retry;

    public void increaseRetries(int maxTry) {
        retry = retry >= maxTry ? 1 : retry + 1;
        this.updated = LocalDateTime.now();
    }
}
