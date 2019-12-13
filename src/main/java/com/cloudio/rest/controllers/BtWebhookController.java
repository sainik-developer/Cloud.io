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

@Log4j2
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class BtWebhookController {

    private final BraintreeGateway gateway;
    private final PaymentService paymentService;

    @PostMapping(value = "/failed", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void someMethod(BrainTreeWebHookRequestDTO webHookRequestDTO) {
        log.info("Braintree webhook is called with bt_signature={} and bt_payload={}", webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload());
        final WebhookNotification webhookNotification = gateway.webhookNotification().parse(webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload());
        log.info("Webhook notification kind is {}", webhookNotification.getKind());
        log.info("Webhook notification details are {}", webhookNotification.toString());
    }
}
