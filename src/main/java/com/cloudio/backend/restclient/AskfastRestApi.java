package com.cloudio.backend.restclient;

import org.springframework.stereotype.Service;

@Service
public interface AskfastRestApi {

    boolean sendSms(final String phoneNumber, final String smsCode);
}
