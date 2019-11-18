package com.cloudio.backend.utils;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class Properties {

    @Value("${askfast.base.url}")
    private String askfastBaseUrl;

    @Value("${askfast.accountId}")
    private String askfastAccountId;

    @Value("${askfast.refreshToken}")
    private String askfastRefreshToken;

    private final  String askfastAuthUrl = "/keyserver/access";

    public final static String START_DIALOG_URL = "/startDialog";

    @Value("${application_cool_off_in_mins_for_retries}")
    private Integer application_cool_off_in_mins_for_retries;

    @Value("${application_max_try}")
    private Integer application_max_try;
    @Value("${mongo_cluster_uri}")
    private String mongo_cluster_uri;
    @Value("${mongo_db_name}")
    private String mongo_db_name;



}
