package com.cloudio.rest.service;

import com.cloudio.rest.dto.TwilioTokenResponseDTO;
import com.cloudio.rest.entity.CompanyDO;
import com.cloudio.rest.exception.CallTransferFailedException;
import com.cloudio.rest.exception.HoldingNotAllowedException;
import com.cloudio.rest.pojo.CompanySetting;
import com.cloudio.rest.pojo.RingType;
import com.cloudio.rest.repository.CompanyRepository;
import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VoiceGrant;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Log4j2
@Service
@RequiredArgsConstructor
public class TwilioService {
    private final AccountService accountService;
    private final CompanyRepository companyRepository;
    private final ReactiveRedisOperations<String, CompanySetting> redisOperations;

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
                .map(Call::getSid)
                .switchIfEmpty(Mono.error(HoldingNotAllowedException::new));
    }

    private String prepareCallHoldingTwilioResponse() {
        return new VoiceResponse.Builder().say(new Say.Builder("Please wait for the call, it will be transferred to person who will resolve the issue").build()).play(new Play.Builder(WAITING_AUDIO_URL).loop(100).build()).build().toXml();
    }

    public Mono<String> transferCall(final String callSid, final String toAccountId) {
        return Mono.fromSupplier(() -> Call.updater(ACCOUNT_SID, callSid).setTwiml(prepareCallTransferResponse(toAccountId)).update())
                .doOnNext(call -> log.info("call is transferred successfully and details are {}", call))
                .map(Call::getSid)
                .switchIfEmpty(Mono.error(CallTransferFailedException::new));
    }

    private String prepareCallTransferResponse(final String toAccountId) {
        return new VoiceResponse.Builder().dial(new Dial.Builder().client(new Client.Builder(createTwilioCompatibleClientId(toAccountId)).build()).build()).build().toXml();
    }

    public Mono<String> handleWithSetting(final String adapterNumber) {
        return companyRepository.findByAdapterNumber(adapterNumber)
                .doOnNext(companyDo -> log.info("adapter number is found and related company {}", companyDo))
                .map(companyDo -> {
                    if (companyDo.getCompanySetting() == null) {
                        companyDo.setCompanySetting(CompanySetting.builder().ringType(RingType.ALL_AT_ONCE).isVoiceMessage(false).build());
                    }
                    return companyDo;
                })
                .flatMap(companyDo -> companyDo.getCompanySetting().getRingType() == RingType.ALL_AT_ONCE ? handleCallAtOnce(companyDo) : handleCallOneByOne(companyDo))
                .switchIfEmpty(Mono.just(new VoiceResponse.Builder()
                        .say(new Say.Builder("Thanks for calling to Cloud.io but No company is associated with number").build())
                        .hangup(new Hangup.Builder().build()).build().toXml()));
    }

    private Mono<String> handleCallAtOnce(final CompanyDO companyDO) {
        return Mono.just(companyDO)
                .flatMap(companyDo -> accountService.getTokenRegisteredAccount(companyDo.getCompanyId())
                        .map(accountId -> new Client.Builder().identity(this.createTwilioCompatibleClientId(accountId)).build())
                        .collectList()
                        .filter(clients -> clients.size() > 0)
                        .doOnNext(clients -> log.info("total number of clients are {}", clients.size()))
                        .map(clients -> {
                            final Dial.Builder builder = new Dial.Builder();
                            builder.method(HttpMethod.GET)
                                    .timeout(calculateTimeout(companyDO.getCompanySetting(), 0))
                                    .action(prepareTimeOutUrl(companyDO.getAdapterNumber(), RingType.ALL_AT_ONCE, 0));
                            clients.forEach(builder::client);
                            return builder.build();
                        })
                        .doOnNext(dial -> redisOperations.opsForValue()
                                .set(companyDO.getAdapterNumber(), companyDO.getCompanySetting(), Duration.ofSeconds(100))
                                .doOnNext(aBoolean -> log.info("data saved in redis status is {}", aBoolean))
                                .subscribe())
                        .map(dial -> new VoiceResponse.Builder().dial(dial).build())
                        .map(VoiceResponse::toXml)
                        .doOnNext(xml -> log.info("dial Twilio xml is {}", xml)))
                .switchIfEmpty(Mono.just(new VoiceResponse.Builder().say(new Say.Builder("Thank you for calling. At this moment no one is available, Please try again at another moment.").build()).hangup(new Hangup.Builder().build()).build().toXml()));
    }

    private Mono<String> handleCallOneByOne(final CompanyDO companyDO) {
        return Mono.just(companyDO)
                .flatMap(companyDo -> accountService.getTokenRegisteredOnlineAndActiveAccount(companyDo.getCompanySetting().getRingOrderAccountIds())
                        .collectList()
                        .doOnNext(strings -> log.info("number of registered user are {}", strings.size()))
                        .doOnNext(strings -> {
                            companyDo.getCompanySetting().setRingOrderAccountIds(strings);
                            redisOperations.opsForValue().set(companyDO.getAdapterNumber(), companyDo.getCompanySetting(), Duration.ofSeconds(companyDo.getCompanySetting().getOrderDelayInSec()
                                    * companyDo.getCompanySetting().getRingOrderAccountIds().size() + 50)).subscribe();
                        })
                        .map(accountIds -> Pair.of(accountIds.get(0), 1)))
                .map(firstAndSecondAccountIds -> Pair.of(new Client.Builder()
                        .identity(this.createTwilioCompatibleClientId(firstAndSecondAccountIds.getLeft())).build(), firstAndSecondAccountIds.getRight()))
                .map(clientNextAccountIdPair -> new Dial.Builder().client(clientNextAccountIdPair.getLeft()).method(HttpMethod.GET).timeout(calculateTimeout(companyDO.getCompanySetting(), 0))
                        .action(prepareTimeOutUrl(companyDO.getAdapterNumber(), RingType.IN_ORDER, clientNextAccountIdPair.getRight())).build())
                .map(dial -> new VoiceResponse.Builder().dial(dial).build())
                .map(VoiceResponse::toXml)
                .doOnNext(xml -> log.info("dial Twilio xml is {}", xml))
                .switchIfEmpty(Mono.just(new VoiceResponse.Builder().say(new Say.Builder("Thank you for calling. At this moment no one is available, Please try again at another moment.").build())
                        .hangup(new Hangup.Builder().build()).build().toXml()));
    }

    public Mono<String> handleOneByOneTimeout(final String adapterNumber, final int nextIndex, final CompanySetting companySetting) {
        log.info("handle one by one timeout is called with setting adapter number {}, nextIndex {} and companySetting {}", adapterNumber, nextIndex, companySetting);
        return Mono.just(companySetting)
                .filter(companySettings -> companySettings.getRingOrderAccountIds().size() > nextIndex)
                .map(companySettings -> companySettings.getRingOrderAccountIds().get(nextIndex))
                .map(nextAccountId -> new Dial.Builder().client(new Client.Builder().identity(this.createTwilioCompatibleClientId(nextAccountId)).build())
                        .method(HttpMethod.GET)
                        .timeout(calculateTimeout(companySetting, nextIndex + 1))
                        .action(prepareTimeOutUrl(adapterNumber, RingType.IN_ORDER, nextIndex + 1)).build())
                .map(dial -> new VoiceResponse.Builder().dial(dial).build())
                .map(VoiceResponse::toXml)
                .switchIfEmpty(handleVoiceMessage(adapterNumber, companySetting));
    }

    public Mono<String> handleVoiceMessage(final String adapterNumber, final CompanySetting companySetting) {
        return Mono.just(companySetting)
                .filter(CompanySetting::getIsVoiceMessage)
                .map(companySettings -> new VoiceResponse.Builder().say(new Say.Builder(companySetting.getVoiceMessageSetting().getContent())
                        .voice(companySetting.getVoiceMessageSetting().getVoiceType().equals("MAN") ? Say.Voice.MAN : Say.Voice.WOMAN)
                        .language(companySetting.getVoiceMessageSetting().getLang().equals("ENGLISH") ? Say.Language.EN_US : Say.Language.NL_NL).build()).build())
                .map(VoiceResponse::toXml)
                .doOnNext(s -> redisOperations.opsForValue().delete(adapterNumber).subscribe())
                .switchIfEmpty(Mono.just(new VoiceResponse.Builder().hangup(new Hangup.Builder().build()).build().toXml()));
    }

    private int calculateTimeout(final CompanySetting companySetting, int index) {
        if (companySetting != null && companySetting.getRingType() == RingType.ALL_AT_ONCE && companySetting.getIsVoiceMessage()) {
            return companySetting.getVoiceMessageSetting().getPlayAfterInSec();
        } else if (companySetting != null && companySetting.getRingType() == RingType.IN_ORDER && companySetting.getIsVoiceMessage()) {
            return companySetting.getVoiceMessageSetting().getAfterLastColleagueInList() ? companySetting.getOrderDelayInSec() :
                    Math.min(companySetting.getVoiceMessageSetting().getPlayAfterInSec() - companySetting.getOrderDelayInSec() * index, companySetting.getOrderDelayInSec());
        }
        return 30;
    }

    private String prepareTimeOutUrl(final String adapterNumber, final RingType ringType, final int index) {
        return "/twilio/voice/timeout?" + "adapterNumber=" + adapterNumber + "&ring_type=" + ringType + "&next_index=" + index;
    }
}
