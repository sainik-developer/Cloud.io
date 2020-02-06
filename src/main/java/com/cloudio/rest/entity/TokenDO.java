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
@Document(value = "tokens")
@NoArgsConstructor
@AllArgsConstructor
public class TokenDO {
    @Id
    private String id;

    private String accountId;
    private String token;
    private String device;
    @LastModifiedDate
    private LocalDateTime stamp;
}
