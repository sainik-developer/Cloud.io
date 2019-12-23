package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.PaymentClientTokenResponseDTO;
import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.dto.SubscriptionRequestDTO;
import com.cloudio.rest.exception.BrainTreeTokenException;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.service.PaymentService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

    @ApiResponses(value = {
            @ApiResponse(code = 200, response = PaymentClientTokenResponseDTO.class, message = "Bt client token is fetched successfully"),
            @ApiResponse(code = 400, response = ResponseDTO.class, message = "AccountId invalid or account's first name or last name are not present, or some problem creating entry in vault or generating client token"),
    })
    @GetMapping("/token")
    Mono<PaymentClientTokenResponseDTO> getClientToken(@RequestHeader("accountId") final String accountId) {
        return accountRepository.findByAccountIdAndStatusAndType(accountId, AccountStatus.ACTIVE, AccountType.ADMIN)
                .flatMap(paymentService::getClientToken)
                .switchIfEmpty(Mono.error(new BrainTreeTokenException("AccountId invalid or account's first name or last name are not present")));
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, response = ResponseDTO.class, message = "Qring subscription request is created successfully"),
            @ApiResponse(code = 400, response = ResponseDTO.class, message = "AccountId invalid or account's first name or last name are not present, or some problem creating entry in vault or generating client token"),
    })
    @PostMapping("/subscribe")
    Mono<ResponseDTO> subscribe(@RequestHeader("accountId") final String accountId, @Validated @RequestBody SubscriptionRequestDTO transactionDTO) {
        return paymentService.subscribe(accountId, transactionDTO)
                .map(s -> ResponseDTO.builder().message(s).build());
    }
}
