package com.cloudio.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(value = "tokenstats")
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatsDO {

    @JsonIgnore
    @Id
    private String id;

    private String batchId;

    private String status;
    private String token;

    @JsonIgnore
    private String notificationId;
    private String extraReason;
    private String pushType;

    @JsonIgnore
    private String accountId;
    private String accountName;
    private String phoneNumber;

    private String payload;
    private String actionType;


    @CreatedDate
    private LocalDateTime creatTime;
    @LastModifiedDate
    private LocalDateTime updatedTime;

}
