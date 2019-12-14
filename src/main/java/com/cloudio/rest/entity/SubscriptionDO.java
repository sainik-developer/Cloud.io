package com.cloudio.rest.entity;

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
@Document("subscriptions")
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDO {
    @Id
    private String id;

    private String accountId;
    private String companyId;
    private String btSubscriptionId;
    private String btPlanId;
    private String status;
    @CreatedDate
    private LocalDateTime created;
    @LastModifiedDate
    private LocalDateTime updated;
}
