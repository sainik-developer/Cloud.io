package com.cloudio.rest.service;

import com.cloudio.rest.dto.TwilioTokenResponseDTO;
import com.twilio.jwt.client.ClientCapability;
import com.twilio.jwt.client.IncomingClientScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Log4j2
@Service
@RequiredArgsConstructor
public class TwilioService {
    @Value("${twilio.accountSID}")
    private String ACCOUNT_SID;
    @Value("${twilio.accessTokenSecret}")
    private String AUTH_TOKEN;

    public Mono<TwilioTokenResponseDTO> generateTwilioClientCapabilityToken(final String accountId) {
        return Mono.fromSupplier(() -> new ClientCapability.Builder(ACCOUNT_SID, AUTH_TOKEN)
                .scopes(Collections.singletonList(new IncomingClientScope(accountId)))
                .build())
                .map(ClientCapability::toJwt)
                .map(capabilityToken -> TwilioTokenResponseDTO.builder().clientCapabilityToken(capabilityToken).build());
    }
}
