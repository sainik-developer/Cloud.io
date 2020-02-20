package com.cloudio.rest.controllers;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.WebhookNotification;
import com.cloudio.rest.dto.BrainTreeWebHookRequestDTO;
import com.cloudio.rest.entity.TransactionDO;
import com.cloudio.rest.repository.SubscriptionRepository;
import com.cloudio.rest.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Log4j2
@RestController
@RequestMapping("/webhook/subscription")
@RequiredArgsConstructor
public class BtWebhookController {
    private final BraintreeGateway gateway;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;

    @PostMapping(value = "/unsuccessfully", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void chargedUnsuccessfully(final BrainTreeWebHookRequestDTO webHookRequestDTO) {
        log.info("Braintree webhook subscription charged Unsuccessfully is called");
        Mono.just(gateway.webhookNotification().parse(webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload()))
                .doOnNext(webhookNotification -> log.info("Webhook notification kind is {}", webhookNotification.getKind()))
                .filter(webhookNotification -> webhookNotification.getKind() == WebhookNotification.Kind.SUBSCRIPTION_CHARGED_UNSUCCESSFULLY)
                .map(WebhookNotification::getSubscription)
                .doOnNext(subscription -> log.debug("transaction details are {}", subscription.getTransactions().get(0)))
                .map(subscription -> TransactionDO.builder().btSubscriptionId(subscription.getId()).btPlanId(subscription.getPlanId()).amount(subscription.getTransactions().get(0).getAmount()).status(WebhookNotification.Kind.SUBSCRIPTION_CHARGED_UNSUCCESSFULLY.toString()).btTransactionId(subscription.getTransactions().get(0).getId()).build())
                .flatMap(transactionDo -> subscriptionRepository.findByBtSubscriptionId(transactionDo.getBtSubscriptionId())
                        .map(subscriptionDO -> {
                            transactionDo.setAccountId(subscriptionDO.getAccountId());
                            return transactionDo;
                        }).switchIfEmpty(Mono.just(transactionDo)))
                .flatMap(transactionRepository::save)
                .subscribe();
    }

    @PostMapping(value = "/successfully", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void chargedSuccessfully(final BrainTreeWebHookRequestDTO webHookRequestDTO) {
        log.info("Braintree webhook subscription charged Successfully is called");
        Mono.just(gateway.webhookNotification().parse(webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload()))
                .doOnNext(webhookNotification -> log.info("Webhook notification kind is {}", webhookNotification.getKind()))
                .filter(webhookNotification -> webhookNotification.getKind() == WebhookNotification.Kind.SUBSCRIPTION_CHARGED_SUCCESSFULLY)
                .map(WebhookNotification::getSubscription)
                .doOnNext(subscription -> log.debug("transaction details are {}", subscription.getTransactions().get(0)))
                .map(subscription -> TransactionDO.builder().btSubscriptionId(subscription.getId()).btPlanId(subscription.getPlanId()).amount(subscription.getTransactions().get(0).getAmount()).status(WebhookNotification.Kind.SUBSCRIPTION_CHARGED_SUCCESSFULLY.toString()).btTransactionId(subscription.getTransactions().get(0).getId()).build())
                .flatMap(transactionDo -> subscriptionRepository.findByBtSubscriptionId(transactionDo.getBtSubscriptionId())
                        .map(subscriptionDO -> {
                            transactionDo.setAccountId(subscriptionDO.getAccountId());
                            return transactionDo;
                        }).switchIfEmpty(Mono.just(transactionDo)))
                .flatMap(transactionRepository::save)
                .subscribe();
    }

    @PostMapping(value = "/trailended", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void trailended(final BrainTreeWebHookRequestDTO webHookRequestDTO) {
        log.info("Braintree webhook subscription trial ended is called");
        Mono.just(gateway.webhookNotification().parse(webHookRequestDTO.getBt_signature(), webHookRequestDTO.getBt_payload()))
                .doOnNext(webhookNotification -> log.info("Webhook notification kind is {}", webhookNotification.getKind()))
                .filter(webhookNotification -> webhookNotification.getKind() == WebhookNotification.Kind.SUBSCRIPTION_TRIAL_ENDED)
                .map(WebhookNotification::getSubscription)
                .doOnNext(subscription -> log.debug("transaction details are {}", subscription.getTransactions().get(0)))
                .map(subscription -> TransactionDO.builder().btSubscriptionId(subscription.getId()).btPlanId(subscription.getPlanId()).amount(BigDecimal.ZERO).status(WebhookNotification.Kind.SUBSCRIPTION_TRIAL_ENDED.toString()).build())
                .flatMap(transactionDo -> subscriptionRepository.findByBtSubscriptionId(transactionDo.getBtSubscriptionId())
                        .map(subscriptionDO -> {
                            transactionDo.setAccountId(subscriptionDO.getAccountId());
                            return transactionDo;
                        }).switchIfEmpty(Mono.just(transactionDo)))
                .flatMap(transactionRepository::save)
                .subscribe();
    }
}
