package com.cloudio.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FirebaseToken {

    public static final String COLLECTION_NAME = "firebasetokens";

    private String accountId;

}
