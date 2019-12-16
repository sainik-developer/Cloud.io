package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.PaymentClientTokenResponseDTO;
import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.dto.SubscriptionRequestDTO;
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
public class BtPaymentController {

    private final PaymentService paymentService;
    private final AccountRepository accountRepository;

    @GetMapping("/token")
    Mono<PaymentClientTokenResponseDTO> getClientToken(@RequestHeader("accountId") final String accountId) {
        return accountRepository.findByAccountIdAndStatusAndType(accountId, AccountStatus.ACTIVE, AccountType.ADMIN)
                .flatMap(paymentService::getClientToken)
                .switchIfEmpty(Mono.error(new BrainTreeTokenException("AccountId invalid or account's first name or last name are not present")));
    }

    @PostMapping("/subscribe")
    Mono<ResponseDTO> subscribe(@RequestHeader("accountId") final String accountId, @Validated @RequestBody SubscriptionRequestDTO transactionDTO) {
        return paymentService.subscribe(accountId, transactionDTO)
                .map(s -> ResponseDTO.builder().message(s).build());
    }
}
