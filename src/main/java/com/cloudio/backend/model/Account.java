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
public class Account {

    public static final String COLLECTION_NAME = "accounts";

    private String accountId;
    private String companyId;
    private String phoneNumber;
    private String name;
    private LocalDateTime updated;
    private String firebaseAuthToken;
    private final AccountStatus status = AccountStatus.ACTIVE;// ACTIVE, INACTIVE, INVITED
    private AccountType type; // ADMIN, MEMBER;

}
