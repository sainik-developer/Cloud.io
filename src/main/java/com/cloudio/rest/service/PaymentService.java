package com.cloudio.rest.service;

import com.amazonaws.util.StringUtils;
import com.braintreegateway.*;
import com.cloudio.rest.dto.PaymentClientTokenResponseDTO;
import com.cloudio.rest.dto.SubscriptionRequestDTO;
import com.cloudio.rest.dto.TransactionDTO;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.entity.SubscriptionDO;
import com.cloudio.rest.exception.SubscriptionException;
import com.cloudio.rest.exception.SuspiciousStateException;
import com.cloudio.rest.mapper.TransactionMapper;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.SubscriptionRepository;
import com.cloudio.rest.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${payment.bt.planId}")
    private String planId;

    private final BraintreeGateway gateway;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final SubscriptionRepository subscriptionRepository;

    public Mono<PaymentClientTokenResponseDTO> getClientToken(final AccountDO accountDO) {
        return Mono.just(accountDO)
                .filter(accountDo -> !StringUtils.isNullOrEmpty(accountDo.getFirstName()) && !StringUtils.isNullOrEmpty(accountDo.getLastName()))
                .flatMap(this::createCustomerInVault)
                .map(accountDo -> new ClientTokenRequest().customerId(accountDO.getDetail().getCustomerId()))
                .map(clientTokenRequest -> PaymentClientTokenResponseDTO
                        .builder()
                        .token(gateway.clientToken().generate(clientTokenRequest))
                        .build());
    }

    public Mono<AccountDO> createCustomerInVault(final AccountDO accountDO) {
        return Mono.just(accountDO)
                .filter(accountDo -> Objects.isNull(accountDo.getDetail()))
                .switchIfEmpty(Mono.just(new CustomerRequest().firstName(accountDO.getFirstName()).lastName(accountDO.getLastName()).phone(accountDO.getPhoneNumber()))
                        .map(gateway.customer()::create)
                        .filter(Result::isSuccess)
                        .map(customerResult -> customerResult.getTarget().getId())
                        .map(brainTreeCustomerId -> {
                            accountDO.getDetail().setCustomerId(brainTreeCustomerId);
                            return accountDO;
                        })
                        .flatMap(accountRepository::save));
    }

    public Mono<String> subscribe(final String accountId, final SubscriptionRequestDTO subscriptionRequestDTO) {
        return accountRepository.findByAccountIdAndStatusAndType(accountId, AccountStatus.ACTIVE, AccountType.ADMIN)
                .flatMap(accountDo -> subscriptionRepository.findByAccountId(accountId))
                .map(subscriptionDo -> Pair.of(subscriptionDo, updateBtSubscription(subscriptionDo, subscriptionRequestDTO.getNonse())))
                .map(subscriptionPair -> {
                    subscriptionPair.getFirst().setStatus(subscriptionPair.getSecond().getStatus().toString());
                    return subscriptionPair.getFirst();
                })
                .flatMap(subscriptionRepository::save)
                .map(subscriptionDO -> "Subscription is updated with nonse for account = " + subscriptionDO.getAccountId())
                .switchIfEmpty(accountRepository.findByAccountIdAndStatusAndType(accountId, AccountStatus.ACTIVE, AccountType.ADMIN)
                        .map(accountDO -> Pair.of(accountDO, createBtSubscription(subscriptionRequestDTO.getNonse())))
                        .map(pair -> SubscriptionDO.builder().accountId(pair.getFirst().getAccountId()).btPlanId(planId).btSubscriptionId(pair.getSecond().getId()).companyId(pair.getFirst().getAccountId()).status(pair.getSecond().getStatus().toString()).build())
                        .flatMap(subscriptionRepository::save)
                        .map(subscriptionDo -> "Subscription is created with nonse for account = " + subscriptionDo.getAccountId())
                        .switchIfEmpty(Mono.error(new SuspiciousStateException())));
    }

    private Subscription updateBtSubscription(final SubscriptionDO subscriptionDO, final String nonse) {
        Result<Subscription> result = gateway.subscription().update(subscriptionDO.getBtSubscriptionId(), new SubscriptionRequest().paymentMethodNonce(nonse).planId(planId));
        if (result.isSuccess()) {
            return result.getSubscription();
        } else {
            throw new SubscriptionException(result.getMessage());
        }
    }

    private Subscription createBtSubscription(final String nonse) {
        Result<Subscription> result = gateway.subscription().create(new SubscriptionRequest().paymentMethodNonce(nonse).planId(planId));
        if (result.isSuccess()) {
            return result.getSubscription();
        } else {
            throw new SubscriptionException(result.getMessage());
        }
    }

}
