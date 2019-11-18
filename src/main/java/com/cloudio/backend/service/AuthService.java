package com.cloudio.backend.service;

import com.cloudio.backend.exception.AccountNotFoundException;
import com.cloudio.backend.exception.SignInException;
import com.cloudio.backend.exception.VerificationException;
import com.cloudio.backend.model.*;
import com.cloudio.backend.repository.*;
import com.cloudio.backend.restclient.AskfastRestApi;
import com.cloudio.backend.utils.Properties;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Log4j2
@Component
@RequiredArgsConstructor
public class AuthService {

    private final SignInCodeRepository signInCodeRepository;
    private final AccountRepository accountRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final AskfastRestApi askfastRestApi;
    private final FirebaseTokenRepository firebaseTokenRepository;
    private final CompanyRepository companyRepository;
    private final Properties properties;



    public String signup(final String phoneNumber) {
        try {
            final String formattedNumber = getFormattedNumber(phoneNumber);
            log.info("Formatted number is {}", formattedNumber);
            return signInCodeRepository.findByPhoneNumber(formattedNumber)
                    .map(signInDetails -> {
                        log.info("Number is already used to sign up {}", signInDetails.getPhoneNumber());
                        if (signInDetails.getRetry() >= properties.getApplication_max_try() &&
                                Duration.between(signInDetails.getUpdated(), LocalDateTime.now()).toMinutes() < properties.getApplication_cool_off_in_mins_for_retries()) {
                            log.error("Sign up is failed as retry count is " + signInDetails.getRetry() + "last updated" + signInDetails.getUpdated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            throw new SignInException("Phone number is locked. You cannot receive code anymore. Please try after sometime.");
                        } else {
                            signInDetails.increaseRetries(properties.getApplication_max_try());
                            if (signupEntry(signInDetails) && askfastRestApi.sendSms(signInDetails.getPhoneNumber(), signInDetails.getSmsCode())) {
                                return "Sms is sent where retry count is {}" + signInDetails.getRetry();
                            }
                            throw new SignInException("Something went wrong with sending SMS,Please try after sometime.");
                        }
                    })
                    .orElseGet(() -> {
                        log.info("Number had arrived for first time to sign up");
                        final SignInDetails signInDetails = SignInDetails.builder().phoneNumber(phoneNumber).retry(1).smsCode(generateSMSCode()).updated(LocalDateTime.now()).build();
                        if (signupEntry(signInDetails) && askfastRestApi.sendSms(signInDetails.getPhoneNumber(), signInDetails.getSmsCode())) {
                            log.info(formattedNumber + " is registered successfully");
                            return "Sms is sent for first time";
                        }
                        log.error("Sms sending is failed for phone number {}", formattedNumber);
                        throw new SignInException("Sms sending is failed");
                    });
        } catch (final NumberParseException e) {
            log.error("There is exception in formatting the number {}", phoneNumber);
            throw new SignInException("Incorrect Phone number provided: " + phoneNumber);
        }
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
    public List<Company> verify(final String phoneNumber, final String code) {
        try {
            log.info("verification start for phone number {} code is {}", phoneNumber, code);
            return signInCodeRepository.findByPhoneNumber(getFormattedNumber(phoneNumber))
                    .filter(signInDetails -> signInDetails.getSmsCode().equals(code))
                    .map(signInDetails -> retrieveAllAssociatedCompanyDetails(phoneNumber))
                    .orElseThrow(() ->
                            new VerificationException("Phone number is not found or code is not matched"));
        } catch (final NumberParseException e) {
            log.error("Phone number formatting is failed for {}", phoneNumber);
            throw new VerificationException("Phone number is incorrect");
        }
    }


    public boolean isValidToken(final String tempToken) {

            final TempAuthToken authToken = decodeTempAuthToken(tempToken);

        try {
            return signInCodeRepository.findByPhoneNumber(getFormattedNumber(authToken.getPhoneNumber()))
                    .filter(signInDetails -> signInDetails.getSmsCode().equals(authToken.getCode()))
                    .isPresent();
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String createTemporaryToken(final String phoneNumber, final String code) {
        return Base64.encodeBase64String((phoneNumber + "#" + code + "#" + LocalDateTime.now()).getBytes());
    }



    public String login(final String tempAuthTokenStr, final String companyId) {
        try {
            final TempAuthToken authToken = decodeTempAuthToken(tempAuthTokenStr);
            log.info("auth token is "+authToken);
            return signInCodeRepository.findByPhoneNumber(getFormattedNumber(authToken.getPhoneNumber()))
                    .filter(signInDetails -> signInDetails.getSmsCode().equals(authToken.getCode()))
                    .map(signInDetails -> accountRepository.findByPhoneNumberAndCompanyId(authToken.getPhoneNumber(), companyId)
                            .map(account -> {
                                log.info("Account is already registered so will get access token for phone number {}", authToken.getPhoneNumber());
                                return getAccessToken(account.getAccountId());
                            }).orElseGet(() -> {
                                log.info("First time registration for phone number {}", authToken.getPhoneNumber());
                                final Account account = Account.builder().accountId("CLOUDIO:ACC:" + UUID.randomUUID().toString()).companyId(companyId).phoneNumber(authToken.getPhoneNumber()).updated(LocalDateTime.now()).build();
                                accountRepository.upsert(account);
                                return getAccessToken(account.getAccountId());
                            })).orElseThrow(Exception::new);

        } catch (final Exception e) {
            log.error("Phone number formatting is failed for ", e);
            throw new VerificationException("Phone number is incorrect");
        }
    }


    private TempAuthToken decodeTempAuthToken(final String tempAuthTokenStr) {
        final String[] values = new String(Base64.decodeBase64(tempAuthTokenStr)).split("#");
        return TempAuthToken.builder().phoneNumber(values[0]).code(values[1]).createTime(LocalDateTime.parse(values[2])).build();
    }

    public String logout(final String accessToken) {
        log.info("user trying to logout {}", accessToken);
        return accessTokenRepository.findByToken(accessToken)
                .flatMap(at -> accessTokenRepository.removeByToken(accessToken)
                        .flatMap(b -> firebaseTokenRepository.removeByAccountId(at.getAccountId())))
                .map(aBoolean -> "logged out successfully")
                .orElseThrow(() -> new AccountNotFoundException("logout failed"));
    }

    private String getFormattedNumber(final String phoneNumber) throws NumberParseException {
        return PhoneNumberUtil.getInstance().format(PhoneNumberUtil.getInstance().parse(phoneNumber, null),
                PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    private boolean signupEntry(final SignInDetails signInDetails) {
        return signInCodeRepository.upsert(signInDetails).isPresent();
    }

    private String generateSMSCode() {
        return String.valueOf((int) Math.floor(100000 + Math.random() * 900000));
    }


    private List<Company> retrieveAllAssociatedCompanyDetails(final String phoneNumber) {
        return accountRepository.findByPhoneNumber(phoneNumber)
                .map(accounts -> accounts.stream().map(account -> companyRepository.findByCompanyId(account.getCompanyId()).orElse(null)).collect(Collectors.toList())).orElse(null);
    }



    private String getAccessToken(final String accountId) {
        log.info("Access token for account id {} is going to be retrieve", accountId);
        return accessTokenRepository.findByToken(accountId)
                .map(AccessToken::getToken)
                .orElseGet(() -> {
                    log.info("Access token to be retrieve for account id {} ", accountId);
                    final String token = UUID.randomUUID().toString();
                    accessTokenRepository.upsert(AccessToken.builder().accountId(accountId).token(token).build());
                    log.info("Access token is generated for account id {} ", accountId);
                    return token;
                });
    }

}
