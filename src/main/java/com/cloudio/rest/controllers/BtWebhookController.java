package com.cloudio.rest.controllers;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.WebhookNotification;
import com.cloudio.rest.dto.BrainTreeWebHookRequestDTO;
import com.cloudio.rest.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/webhook/subscription")
@RequiredArgsConstructor
public class BtWebhookController {

    private final BraintreeGateway gateway;
    private final PaymentService paymentService;

    @PostMapping(value = "/unsuccessfully", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void chargedUnsuccessfully(final BrainTreeWebHookRequestDTO webHookRequestDTO) {
        log.info("Braintree webhook charged Unsuccessfully subscription is called with bt_signature={} and bt_payload={}", webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload());
        final WebhookNotification webhookNotification = gateway.webhookNotification().parse(webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload());
        log.info("Webhook notification kind is {}", webhookNotification.getKind());
        log.info("Webhook notification details are {}", webhookNotification.toString());
    }

    @PostMapping(value = "/successfully", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void chargedSuccessfully(final BrainTreeWebHookRequestDTO webHookRequestDTO) {
        log.info("Braintree webhook charged Unsuccessfully subscription is called with bt_signature={} and bt_payload={}", webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload());
        Mono.just(gateway.webhookNotification().parse(webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload()))
                .doOnNext(webhookNotification -> log.info("Webhook notification kind is {}", webhookNotification.getKind()))
                .filter(webhookNotification -> webhookNotification.getKind() == WebhookNotification.Kind.SUBSCRIPTION_CHARGED_SUCCESSFULLY)
                .map(WebhookNotification::getSubscription)
                .map(subscription -> subscription.getTransactions())
                .subscribe();
    }

    @PostMapping(value = "/trailended", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void trailended(final BrainTreeWebHookRequestDTO webHookRequestDTO) {
        log.info("Braintree webhook charged Unsuccessfully subscription is called with bt_signature={} and bt_payload={}", webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload());
        final WebhookNotification webhookNotification = gateway.webhookNotification().parse(webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload());
        log.info("Webhook notification kind is {}", webhookNotification.getKind());
        log.info("Webhook notification details are {}", webhookNotification.toString());
    }

}
