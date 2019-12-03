package com.cloudio.rest.service;

import com.braintreegateway.*;
import com.cloudio.rest.dto.BraintreeTokenDTO;
import com.cloudio.rest.dto.TransactionDTO;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.exception.BrainTreeTokenException;
import com.cloudio.rest.exception.SubscriptionException;
import com.cloudio.rest.exception.SuspiciousStateException;
import com.cloudio.rest.mapper.TransactionMapper;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${cloud.io.payment.planId}")
    private String planId;

    private final BraintreeGateway gateway;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public Mono<BraintreeTokenDTO> getClientToken(final String accountId) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .flatMap(this::createCustomerIfNew)
                .map(accountDO -> new ClientTokenRequest()
                        .customerId(accountDO.getBraintreeCustomerId()))
                .map(clientTokenRequest -> BraintreeTokenDTO
                        .builder()
                        .token(gateway.clientToken().generate(clientTokenRequest))
                        .build())
                .switchIfEmpty(Mono.error(new BrainTreeTokenException("AccountId invalid")));
    }

    private Mono<AccountDO> createCustomerIfNew(final AccountDO accountDO) {
        return Mono.just(accountDO)
                .filter(accountDo -> !Objects.isNull(accountDo.getBraintreeCustomerId()))
                .switchIfEmpty(Mono.just(new CustomerRequest().firstName(accountDO.getFirstName()).lastName(accountDO.getLastName()).phone(accountDO.getPhoneNumber()))
                        .map(gateway.customer()::create)
                        .filter(Result::isSuccess)
                        .map(customerResult -> customerResult.getTarget().getId())
                        .map(brainTreeCustomerId -> {
                            accountDO.setBraintreeCustomerId(brainTreeCustomerId);
                            return accountDO;
                        })
                        .flatMap(accountRepository::save));
    }

    public Mono<TransactionDTO> subscribe(String accountId, TransactionDTO transactionDTO) {
        transactionDTO.setAccountId(accountId);
        return Mono.just(subscribe(transactionDTO))
                .doOnNext(subscription -> log.info("Subscription successful with id {}", subscription.getId()))
                .flatMap(subscription -> transactionRepository.save(TransactionMapper.INSTANCE.fromDTO(transactionDTO)))
                .map(TransactionMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(new SuspiciousStateException()));
    }

    private Subscription subscribe(TransactionDTO transactionDTO) {
        transactionDTO.setPlanId(planId);
        SubscriptionRequest request = new SubscriptionRequest()
                .paymentMethodNonce(transactionDTO.getNonse())
                .planId(transactionDTO.getPlanId());

        Result<Subscription> result = gateway.subscription().create(request);
        if (result.isSuccess()) {
            return result.getSubscription();
        } else {
            throw new SubscriptionException(result.getMessage());
        }
    }

}
