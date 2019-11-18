package com.cloudio.backend.restclient;

import com.askfast.askfastapi.AskFastRestClient;
import com.askfast.model.AdapterType;
import com.askfast.model.Result;
import com.cloudio.backend.utils.Properties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
@Service
public class AskfastRestApiImpl implements AskfastRestApi {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    private Properties properties;

    private final AtomicReference<String> CACHED_ACCESS_TOKEN = new AtomicReference<>(null);
    private final AtomicLong lastCachedTime = new AtomicLong(0);

    public AskfastRestApiImpl() {

    }


    @Override
    public boolean sendSms(final String phoneNumber, final String smsCode) {
        try {
            log.info("properties is "+properties.toString());
            AskFastRestClient askFastRestClient = new AskFastRestClient(properties.getAskfastAccountId(), properties.getAskfastRefreshToken(), null, properties.getAskfastBaseUrl());
            Map<String, String> addressMap = new HashMap<>();
            addressMap.put(phoneNumber, phoneNumber);
            Result rs = null;
            try {
                rs = askFastRestClient.startDialog(addressMap, null, null, AdapterType.SMS, null, "CloudIO", null, "text:// verification code is " + smsCode);
                return rs.getCode() == HttpStatus.SC_OK || rs.getCode() == HttpStatus.SC_CREATED;
            } catch (RetrofitError retrofitError) {
                String retrofitErrorString = new String(((TypedByteArray) retrofitError.getResponse().getBody()).getBytes());
                String logErrorMessage = " Retrofit Response : \n" + retrofitErrorString;
                log.error(logErrorMessage);
            }
        } catch (final Exception e) {
            log.error("Error in sending SMS due to reason {}", e.getMessage());
        }
        return false;
    }

    private interface JSONableRequest {
        default String toJson() {
            try {
                return OBJECT_MAPPER.writeValueAsString(this);
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Data
    @Builder
    public static class SendSmsRequest implements JSONableRequest {
        private String address;
        private String url;
        private final String adapterType = "SMS";
        private final String senderName = "AskFast";
    }

    @Data
    @Builder
    public static class AccessTokenRequest implements JSONableRequest {
        private String accountId;
        private String refreshToken;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessTokenResponse {
        private String version;
        private AccessToken result;
        private String code;
        private String message;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccessToken {
        private String accessToken;
    }
}
