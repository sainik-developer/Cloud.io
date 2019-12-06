package com.cloudio.rest.controllers;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.WebhookNotification;
import com.cloudio.rest.dto.PaymentClientTokenResponseDTO;
import com.cloudio.rest.dto.TransactionDTO;
import com.cloudio.rest.exception.BrainTreeTokenException;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final BraintreeGateway gateway;
    private final PaymentService paymentService;
    private final AccountRepository accountRepository;

    @GetMapping("/token")
    Mono<PaymentClientTokenResponseDTO> getClientToken(@RequestHeader("accountId") final String accountId) {
        return accountRepository.findByAccountIdAndStatusAndType(accountId, AccountStatus.ACTIVE, AccountType.ADMIN)
                .flatMap(paymentService::getClientToken)
                .switchIfEmpty(Mono.error(new BrainTreeTokenException("AccountId invalid")));
    }

    @PostMapping("/subscribe")
    Mono<TransactionDTO> subscribe(@RequestHeader("accountId") final String accountId, @Validated @RequestBody TransactionDTO transactionDTO) {
        return paymentService.subscribe(accountId, transactionDTO);
    }

    @PostMapping("/failed")
    void someMethod(@RequestParam("bt_signature") final String btSignature, @RequestParam("bt_payload") final String btPayload) {
        final WebhookNotification webhookNotification = gateway.webhookNotification().parse(btSignature, btPayload);
        webhookNotification.getSubscription().getId();// subscription ID

    }
}
