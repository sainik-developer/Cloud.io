package com.cloudio.rest.controllers;

import com.braintreegateway.Subscription;
import com.cloudio.rest.dto.BraintreeTokenDTO;
import com.cloudio.rest.dto.TransactionDTO;
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

    private final PaymentService paymentService;

    @GetMapping("/token")
    Mono<BraintreeTokenDTO> getClientToken(@RequestHeader("accountId") final String accountId) {
         return paymentService.getClientToken(accountId);
    }

    @PostMapping("/subscribe")
    Mono<TransactionDTO> subscribe(@RequestHeader("accountId") final String accountId, @Validated @RequestBody TransactionDTO transactionDTO) {
        return paymentService.subscribe(accountId, transactionDTO);
    }
}
