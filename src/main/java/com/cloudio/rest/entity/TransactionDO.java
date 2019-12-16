package com.cloudio.rest.entity;

import io.github.classgraph.json.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
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

    private String accountId;
    private String btPlanId;
    private String btSubscriptionId;
    private String btTransactionId;
    private BigDecimal amount;
    private String status;
    @CreatedDate
    private LocalDateTime created;
}
