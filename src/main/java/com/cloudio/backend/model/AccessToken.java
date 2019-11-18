package com.cloudio.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessToken {

    public static final String COLLECTION_NAME = "accesstokens";

    private String accountId;
    private String token;
    private long stamp;

}
