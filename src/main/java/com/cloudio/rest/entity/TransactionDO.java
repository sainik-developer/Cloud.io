package com.cloudio.rest.entity;

import io.github.classgraph.json.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Document("transactions")
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDO {
    @Id
    private String id;

    private String planId;
    private String subscriptionId;
    private String accountId;
    private BigDecimal amount;
    private String status;
    @CreatedDate
    private LocalDateTime created;
}
