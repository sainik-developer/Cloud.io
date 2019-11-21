package com.cloudio.backend.entity;

import io.github.classgraph.json.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document("accesstokens")
@AllArgsConstructor
@NoArgsConstructor
public class AccessTokenDO {

    @Id
    private String id;

    private String accountId;
    private String token;
    @LastModifiedDate
    private LocalDateTime stamp;

}
