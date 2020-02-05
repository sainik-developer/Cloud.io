package com.cloudio.rest.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(value = "groups")
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSendRequestDO {
    @Id
    private String id;
    private String accountId;
    private String data;
}
