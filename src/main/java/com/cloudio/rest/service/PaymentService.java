package com.cloudio.rest.service;

import com.braintreegateway.*;
import com.cloudio.rest.dto.BrainTreeNonceDTO;
import com.cloudio.rest.dto.BraintreeTokenDTO;
import com.cloudio.rest.exception.SubscriptionException;
import com.cloudio.rest.exception.SuspiciousStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${cloud.io.payment.planId}")
    private String planId;

    private final BraintreeGateway gateway;

    public Mono<BraintreeTokenDTO> getClientToken(String accountId) {
        return Mono.just(BraintreeTokenDTO.builder().token(gateway.clientToken().generate()).build());
    }

    public Mono<Subscription> subscribe(String accountId, BrainTreeNonceDTO brainTreeNonceDTO) {

        return Mono.just(subscribe(brainTreeNonceDTO))
                .switchIfEmpty(Mono.error(new SuspiciousStateException()));

    }

    private Subscription subscribe(BrainTreeNonceDTO brainTreeNonceDTO){
        SubscriptionRequest request = new SubscriptionRequest()
                .paymentMethodToken(brainTreeNonceDTO.getNonseToken())
                .planId(planId);

        Result<Subscription> result = gateway.subscription().create(request);
        if(result.isSuccess()){
           return result.getSubscription();
        }else{
            throw  new SubscriptionException(result.getMessage());
        }
    }

}
