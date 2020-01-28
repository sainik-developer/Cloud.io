package com.cloudio.rest.entity;

import com.cloudio.rest.pojo.AccountState;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.BrainTreeDetail;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
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
    private String profileUrl;
    private String jobTitle;
    private BrainTreeDetail detail; // BrainTree Customer Id & plan Id
    @CreatedDate
    private LocalDateTime created;
    @LastModifiedDate
    private LocalDateTime updated;
    private String firebaseAuthToken;
    private AccountStatus status = AccountStatus.ACTIVE;
    private AccountType type;
    private AccountState state = AccountState.OFFLINE;

    public String getRegionCodeForCountryCode() {
        try {
            return PhoneNumberUtil.getInstance().getRegionCodeForCountryCode(PhoneNumberUtil.getInstance().parse(phoneNumber, null).getCountryCode());
        } catch (final NumberParseException e) {
            return "NL";
        }
    }
}
