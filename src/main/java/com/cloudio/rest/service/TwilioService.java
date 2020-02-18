package com.cloudio.rest.service;

import com.cloudio.rest.dto.TwilioTokenResponseDTO;
import com.cloudio.rest.exception.CallTransferFailedException;
import com.cloudio.rest.exception.HoldingNotAllowedException;
import com.twilio.Twilio;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VoiceGrant;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Client;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Play;
import com.twilio.twiml.voice.Say;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

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
    @Value("${twilio.waiting.audio.url}")
    private String WAITING_AUDIO_URL;

    @PostConstruct
    void initTwilioSdk() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

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
    public String createTwilioCompatibleClientId(final String accountId) {
        if (StringUtils.isEmpty(accountId)) {
            throw new NullPointerException();
        }
        return accountId.replaceAll("-", "_").replaceAll(":", "_");
    }

    public Mono<String> holdIncomingCallToAdapter(final String fromAccount, final String callSid) {
        return Mono.fromSupplier(() -> Call.fetcher(ACCOUNT_SID, callSid).fetch())
                .doOnNext(call -> log.info("fetched call details are {}", call.toString()))
                .doOnNext(call -> log.info("toCallId is {}", call.getTo()))
                .filter(call -> ("client:" + createTwilioCompatibleClientId(fromAccount)).equals(call.getTo()))
                .map(call -> Call.updater(ACCOUNT_SID, call.getParentCallSid()).setTwiml(prepareCallHoldingTwilioResponse()).update())
                .doOnNext(call -> log.info("hold request update details are {}", call))
                .map(Call::getParentCallSid)
                .switchIfEmpty(Mono.error(HoldingNotAllowedException::new));
    }

    private String prepareCallHoldingTwilioResponse() {
        return new VoiceResponse.Builder().say(new Say.Builder("Please wait for the call, it will be transferred to person who will resolve the issue").build()).play(new Play.Builder(WAITING_AUDIO_URL).loop(100).build()).build().toXml();
    }

    public Mono<String> transferCall(final String callSid, final String toAccountId) {
        return Mono.fromSupplier(() -> Call.updater(ACCOUNT_SID, callSid).setTwiml(prepareCallTransferResponse(toAccountId)).update())
                .filter(call -> call.getTo().equals("client" + createTwilioCompatibleClientId(toAccountId)))
                .map(Call::getSid)
                .switchIfEmpty(Mono.error(CallTransferFailedException::new));
    }

    private String prepareCallTransferResponse(final String toAccountId) {
        return new VoiceResponse.Builder().dial(new Dial.Builder().client(new Client.Builder(createTwilioCompatibleClientId(toAccountId)).build()).build()).build().toXml();
    }
}
