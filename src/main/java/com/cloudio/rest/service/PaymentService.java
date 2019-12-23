package com.cloudio.rest.service;

import com.amazonaws.util.StringUtils;
import com.braintreegateway.*;
import com.cloudio.rest.dto.PaymentClientTokenResponseDTO;
import com.cloudio.rest.dto.SubscriptionRequestDTO;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.entity.SubscriptionDO;
import com.cloudio.rest.exception.BrainTreeTokenException;
import com.cloudio.rest.exception.SubscriptionException;
import com.cloudio.rest.exception.SuspiciousStateException;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.BrainTreeDetail;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.SubscriptionRepository;
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
    private final AccountRepository accountRepository;
    private final SubscriptionRepository subscriptionRepository;

    public Mono<PaymentClientTokenResponseDTO> getClientToken(final AccountDO accountDO) {
        return Mono.just(accountDO)
                .filter(accountDo -> !StringUtils.isNullOrEmpty(accountDo.getFirstName()) && !StringUtils.isNullOrEmpty(accountDo.getLastName()))
                .flatMap(this::createCustomerInVault)
                .map(accountDo -> PaymentClientTokenResponseDTO.builder().token(generateClientToken(accountDo.getDetail().getCustomerId())).build());
    }

    private String generateClientToken(final String btCustomerId) {
        try {
            return gateway.clientToken().generate(new ClientTokenRequest().customerId(btCustomerId));
        } catch (RuntimeException e) {
            throw new BrainTreeTokenException("client token generation error due to " + e.getMessage());
        }
    }

    public Mono<AccountDO> createCustomerInVault(final AccountDO accountDO) {
        return Mono.just(accountDO)
                .filter(accountDo -> !Objects.isNull(accountDo.getDetail()))
                .switchIfEmpty(Mono.just(new CustomerRequest().firstName(accountDO.getFirstName()).lastName(accountDO.getLastName()).phone(accountDO.getPhoneNumber()))
                        .map(gateway.customer()::create)
                        .filter(Result::isSuccess)
                        .map(customerResult -> customerResult.getTarget().getId())
                        .map(brainTreeCustomerId -> {
                            accountDO.setDetail(BrainTreeDetail.builder().planId(planId).customerId(brainTreeCustomerId).build());
                            return accountDO;
                        })
                        .flatMap(accountRepository::save)
                        .switchIfEmpty(Mono.error(new BrainTreeTokenException("vault entry failed for customer with details" + accountDO.toString()))));
    }

    public Mono<String> subscribe(final String accountId, final SubscriptionRequestDTO subscriptionRequestDTO) {
        log.debug("braintree subscription is called for accountId  {}", accountId);
        return accountRepository.findByAccountIdAndStatusAndType(accountId, AccountStatus.ACTIVE, AccountType.ADMIN)
                .doOnNext(accountDo -> log.debug("accountId = {} found as ADMIN and ACTIVE", accountId))
                .flatMap(accountDo -> subscriptionRepository.findByAccountId(accountId))
                .doOnNext(subscriptionDo -> log.debug("subscription is found as {}", subscriptionDo))
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
        log.debug("update subscription is called with details {}", subscriptionDO);
        Result<Subscription> result = gateway.subscription().update(subscriptionDO.getBtSubscriptionId(), new SubscriptionRequest().paymentMethodNonce(nonse).planId(planId));
        if (result.isSuccess()) {
            log.debug("update is successful");
            return result.getTarget();
        } else {
            log.error("update is failed reason = {}", result.getMessage());
            throw new SubscriptionException(result.getMessage());
        }
    }

    private Subscription createBtSubscription(final String nonse) {
        log.debug("create subscription is called with details");
        Result<Subscription> result = gateway.subscription().create(new SubscriptionRequest().paymentMethodNonce(nonse).planId(planId));
        if (result.isSuccess()) {
            log.debug("create of subscription is successful");
            return result.getTarget();
        } else {
            log.error("create of subscription is failed. reason  = {}", result.getMessage());
            throw new SubscriptionException(result.getMessage());
        }
    }
}
