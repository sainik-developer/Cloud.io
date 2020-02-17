package com.cloudio.rest.service;

import com.cloudio.rest.dto.TwilioTokenResponseDTO;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VoiceGrant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class TwilioService {
    @Value("${twilio.account.sid}")
    private String ACCOUNT_SID;
    @Value("${twilio.account.authtoken}")
    private String AUTH_TOKEN;
    @Value("${twilio.api_key}")
    private String TWILIO_API_KEY;
    @Value("${twilio.api_secret}")
    private String TWILIO_API_SECRET;
    @Value("${twilio.accessToken.ttl.sec}")
    private Integer ACCESS_TOKEN_TTL;
    @Value("${twilio.applicationSID}")
    private String APPLICATION_ID;
    @Value("${twilio.push.credential.fcm.id}")
    private String fcmPushId;
    @Value("${twilio.push.credential.apn.id}")
    private String apnPushId;

    public Mono<TwilioTokenResponseDTO> generateTwilioAccessToken(final String accountId, final String deviceType) {
        return Mono.fromSupplier(() -> new AccessToken.Builder(ACCOUNT_SID, TWILIO_API_KEY, TWILIO_API_SECRET)
                .identity(createTwilioCompatibleClientId(accountId))
                .grant(new VoiceGrant().setIncomingAllow(true).setOutgoingApplicationSid(APPLICATION_ID).setPushCredentialSid(deviceType.equals("ios") ? apnPushId : fcmPushId))
                .ttl(ACCESS_TOKEN_TTL)
                .build())
                .map(AccessToken::toJwt)
                .map(capabilityToken -> TwilioTokenResponseDTO.builder().clientCapabilityToken(capabilityToken).build());
    }

    /***
     * https://www.twilio.com/docs/voice/twiml/client
     * The client identifier currently may only contain alpha-numeric and underscore characters.
     * @param accountId
     * @return
     */
    private String createTwilioCompatibleClientId(final String accountId) {
        if (StringUtils.isEmpty(accountId)) {
            throw new NullPointerException();
        }
        return accountId.replaceAll("-", "_").replaceAll(":", "_");
    }
}
