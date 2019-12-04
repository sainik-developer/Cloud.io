package com.cloudio.rest.entity;

import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.AskfastDetail;
import io.github.classgraph.json.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document("accounts")
@AllArgsConstructor
@NoArgsConstructor
public class AccountDO {

    @Id
    private String id;

    private String accountId;
    private String companyId;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String profileUrl;
    @CreatedDate
    private LocalDateTime created;
    @LastModifiedDate
    private LocalDateTime updated;
    private String firebaseAuthToken;
    private AccountStatus status = AccountStatus.ACTIVE;
    private AccountType type;
    private AskfastDetail askfastDetail;

}
