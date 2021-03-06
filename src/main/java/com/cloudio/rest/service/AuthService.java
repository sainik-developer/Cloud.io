package com.cloudio.rest.service;

import com.cloudio.rest.dto.LoginResponseDTO;
import com.cloudio.rest.entity.AccessTokenDO;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.entity.CompanyDO;
import com.cloudio.rest.entity.SignInDetailDO;
import com.cloudio.rest.exception.InvalidTempTokenException;
import com.cloudio.rest.exception.SignInException;
import com.cloudio.rest.exception.SuspiciousStateException;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.OTPGenerator;
import com.cloudio.rest.pojo.TempAuthToken;
import com.cloudio.rest.repository.*;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Log4j2
@Component
@RequiredArgsConstructor
public class AuthService {
    private final SignInCodeRepository signInCodeRepository;
    private final AccountRepository accountRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final AskFastService askFastService;
    private final CompanyRepository companyRepository;
    private final OTPGenerator otpGenerator;
    private final TokenRepository tokenRepository;

    @Value("${cloudio.signup.maxRetry}")
    private Integer maxRetry;
    @Value("${cloudio.signup.tempTokenExpireTime.min}")
    private Integer TEMP_TOKEN_SPAN_IN_MIN;
    @Value("${cloudio.signup.cool_of_in_min_for_retries}")
    private Integer COOL_OF_IN_MIN_FOR_RETRIES;

    public Mono<String> signup(final String phoneNumber) {
        return Mono.just(getFormattedNumber(phoneNumber))
                .doOnNext(formattedNumber -> log.info("Formatted number is {}", formattedNumber))
                .flatMap(signInCodeRepository::findByPhoneNumber)
                .map(signInDetailDo -> {
                    if (signInDetailDo.getRetry() >= maxRetry && Duration.between(signInDetailDo.getUpdated(), LocalDateTime.now()).toMinutes() < COOL_OF_IN_MIN_FOR_RETRIES) {
                        log.error("Sign up is failed as retry count is {} and allowed time was {}", signInDetailDo.getRetry(), signInDetailDo.getUpdated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        throw new SignInException("Phone number is locked. You cannot receive code anymore. Please try after sometime.");
                    }
                    signInDetailDo.increaseRetries(maxRetry);
                    signInDetailDo.setSmsCode(otpGenerator.generateSMSCode());
                    return signInDetailDo;
                })
                .flatMap(signInCodeRepository::save)
                .flatMap(signInDetailDo -> askFastService.doAuthAndSendSMS(signInDetailDo.getPhoneNumber(), "Your verification code is " + signInDetailDo.getSmsCode())
                        .filter(Boolean::booleanValue)
                        .map(aBoolean -> "Sms is sent where retry count is " + signInDetailDo.getRetry())
                        .switchIfEmpty(Mono.just("Something went wrong with sending SMS,Please try after sometime.")))
                .switchIfEmpty(Mono.just(SignInDetailDO.builder().phoneNumber(getFormattedNumber(phoneNumber)).retry(1).smsCode(otpGenerator.generateSMSCode()).build())
                        .doOnNext(signInDetailDo -> log.info("Number had arrived for first time to sign up {}", signInDetailDo.getPhoneNumber()))
                        .flatMap(signInCodeRepository::save)
                        .flatMap(signInDetailDo -> askFastService.doAuthAndSendSMS(signInDetailDo.getPhoneNumber(), "Your verification code is " + signInDetailDo.getSmsCode())
                                .filter(Boolean::booleanValue)
                                .doOnNext(aBoolean -> log.info("{} is registered successfully", signInDetailDo.getPhoneNumber()))
                                .map(aBoolean -> "Sms is sent for first time")
                                .switchIfEmpty(Mono.just("Something went wrong with sending SMS,Please try after sometime."))));
    }

    /***
     * /verify POST {phone number + opt }->
     *
     * Response
     * header Authorization : data base Id
     *
     * body List Company  employer list
     *
     *
     * POST /login
     * BODY
     * {login data base Id and company Id  }-> token (mongo based in header)  header Authorization : valid token
     *
     *  Account details
     *
     *
     * @param phoneNumber - phone number
     * @param code - otp
     * @return List of companies
     */
    public Mono<Boolean> verify(final String phoneNumber, final String code) {
        log.info("verification start for phone number {} code is {}", phoneNumber, code);
        return signInCodeRepository.findByPhoneNumber(getFormattedNumber(phoneNumber))
                .doOnNext(signInDetailDo -> {
                    log.info("phone number found for verification {} and code in db is {}",
                            signInDetailDo.getPhoneNumber(), signInDetailDo.getSmsCode());
                })
                .map(signInDetailDO -> signInDetailDO.getSmsCode().equals(code));
    }

    public Mono<Boolean> isValidToken(final String tempToken) {
        final TempAuthToken authToken = decodeTempAuthToken(tempToken);
        return signInCodeRepository.findByPhoneNumber(getFormattedNumber(authToken.getPhoneNumber()))
                .map(signInDetailDO -> signInDetailDO.getSmsCode().equals(authToken.getCode()));
    }

    public String createTemporaryToken(final String phoneNumber, final String code) {
        return Base64.encodeBase64String((phoneNumber + "#" + code + "#" + LocalDateTime.now()).getBytes());
    }

    public Mono<LoginResponseDTO> login(final String tempAuthTokenStr, final String companyId) {
        return Mono.just(decodeTempAuthToken(tempAuthTokenStr))
                .flatMap(this::tokenValid)
                .flatMap(authToken -> signInCodeRepository.findByPhoneNumber(getFormattedNumber(authToken.getPhoneNumber()))
                        .doOnNext(signInDetailDo -> log.info("Phone number is found in signincodes {}", signInDetailDo.getPhoneNumber()))
                        .filter(signInDetailDo -> signInDetailDo.getSmsCode().equals(authToken.getCode()))
                        .doOnNext(signInDetailDo -> log.info("temp token authentication is successful for phoneNumber {}", signInDetailDo.getPhoneNumber()))
                        .doOnNext(signInDetailDo -> signInCodeRepository.delete(signInDetailDo).subscribe())
                        .flatMap(signInDetailDo -> accountRepository.findByPhoneNumberAndCompanyId(authToken.getPhoneNumber(), companyId)
                                .flatMap(accountDO -> {
                                    log.info("Account is already registered, so will get access token for phone number {}", authToken.getPhoneNumber());
                                    return getAccessToken(accountDO.getAccountId())
                                            .map(s -> LoginResponseDTO.builder().authorization(s).build());
                                }).switchIfEmpty(Mono.error(new SuspiciousStateException()))));
    }

    private Mono<TempAuthToken> tokenValid(TempAuthToken authToken) {
        return ChronoUnit.MINUTES.between(authToken.getCreateTime(), LocalDateTime.now()) <= TEMP_TOKEN_SPAN_IN_MIN ?
                Mono.just(authToken) : Mono.error(new InvalidTempTokenException("Temporary token expired"));
    }

    public TempAuthToken decodeTempAuthToken(final String tempAuthTokenStr) {
        try {
            final String[] values = new String(Base64.decodeBase64(tempAuthTokenStr)).split("#");
            return TempAuthToken.builder().phoneNumber(values[0]).code(values[1]).createTime(LocalDateTime.parse(values[2])).build();
        } catch (final Exception e) {
            throw new InvalidTempTokenException("Temp token is invalid");
        }
    }

    public Mono<String> logout(final String accessToken) {
        log.info("user trying to logout {}", accessToken);
        return accessTokenRepository.findByToken(accessToken)
                .flatMap(at -> accessTokenRepository.deleteByToken(at.getToken())
                        .flatMap(b -> accountRepository.findByAccountIdAndStatus(at.getAccountId(), AccountStatus.ACTIVE)
                                .map(accountDo -> {
                                    accountDo.setFirebaseAuthToken(null);
                                    return accountDo;
                                })
                                .flatMap(accountRepository::save)
                                .flatMap(accountDo -> tokenRepository.deleteByAccountId(accountDo.getAccountId()))))
                .map(aBoolean -> "Logged out successfully");
    }

    private String getFormattedNumber(final String phoneNumber) throws SignInException {
        try {
            return PhoneNumberUtil.getInstance().format(PhoneNumberUtil.getInstance().parse(phoneNumber, null),
                    PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (final NumberParseException e) {
            throw new SignInException("Incorrect Phone number provided: " + phoneNumber);
        }
    }

    public Flux<CompanyDO> retrieveAllAssociatedCompanyDetails(final String phoneNumber) {
        return accountRepository.findByPhoneNumber(phoneNumber)
                .map(AccountDO::getCompanyId)
                .flatMap(companyRepository::findByCompanyId);
    }

    private Mono<String> getAccessToken(final String accountId) {
        log.info("Access token for account id {} is going to be retrieved", accountId);
        return accessTokenRepository.findByAccountId(accountId)
                .doOnNext(accessTokenDo -> log.info("Access token is found for accountId {}", accountId))
                .map(accessTokenDo -> {
                    accessTokenDo.setToken(UUID.randomUUID().toString());
                    accessTokenDo.setStamp(LocalDateTime.now());
                    return accessTokenDo;
                })
                .flatMap(accessTokenRepository::save)
                .doOnNext(accessTokenDo -> log.info("New token for accountId {} is {}", accessTokenDo.getAccountId(), accessTokenDo.getToken()))
                .map(AccessTokenDO::getToken)
                .switchIfEmpty(Mono.just(AccessTokenDO.builder().accountId(accountId).stamp(LocalDateTime.now()).token(UUID.randomUUID().toString()).build())
                        .doOnNext(accessTokenDo -> log.info("Access token is generated for account id {} is {}", accountId, accessTokenDo.getToken()))
                        .flatMap(accessTokenRepository::save)
                        .map(AccessTokenDO::getToken));
    }
}
