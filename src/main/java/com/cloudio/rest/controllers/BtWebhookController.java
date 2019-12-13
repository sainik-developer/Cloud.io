package com.cloudio.rest.controllers;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.WebhookNotification;
import com.cloudio.rest.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class BtWebhookController {

    private final BraintreeGateway gateway;
    private final PaymentService paymentService;

    @PostMapping("/failed")
    void someMethod(@RequestParam("bt_signature") final String btSignature, @RequestParam("bt_payload") final String btPayload) {
//        final WebhookNotification webhookNotification = gateway.webhookNotification().parse(btSignature, btPayload);
//        log.info("Webhook notification kind is {}", webhookNotification.getKind());
//        log.info("Webhook notification details are {}", webhookNotification.toString());
//        log.info("");
//        webhookNotification.getSubscription().getId();// subscription ID

        log.info("Braintree bt_signature={} and bt_payload={}", btSignature, btPayload);

    }
}
