package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.PaymentClientTokenResponseDTO;
import com.cloudio.rest.dto.TransactionDTO;
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

    private final PaymentService paymentService;
    private final AccountRepository accountRepository;

//    @GetMapping("/token")
//    Mono<PaymentClientTokenResponseDTO> getClientToken(@RequestHeader("accountId") final String accountId) {
//        accountRepository.findByAccountIdAndStatusAndType(accountId, AccountStatus.ACTIVE, AccountType.ADMIN)
//
//
//        return paymentService.getClientToken(accountId);
//    }
//
//    @PostMapping("/subscribe")
//    Mono<TransactionDTO> subscribe(@RequestHeader("accountId") final String accountId, @Validated @RequestBody TransactionDTO transactionDTO) {
//        return paymentService.subscribe(accountId, transactionDTO);
//    }
}
