package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.NotificationSendRequestDTO;
import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.exception.AccountNotExistException;
import com.cloudio.rest.exception.NotificationException;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.GroupRepository;
import com.cloudio.rest.service.NotificationService;
import com.cloudio.rest.validator.ValidationMarker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Log4j2
@Validated
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final AccountRepository accountRepository;
    private final GroupRepository groupRepository;
    private final NotificationService notificationService;

    @PostMapping(value = "/sendToAccount", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseDTO> sendToAccount(@RequestHeader("accountId") final String accountId,
                                    @Validated(ValidationMarker.AccountIDMandatoryMarker.class)
                                    @RequestBody final NotificationSendRequestDTO notificationSendRequestDTO) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .doOnNext(accountDo -> log.info("account is found ACTIVE and details are {}", accountDo))
                .flatMap(accountDO -> accountRepository.findByAccountId(notificationSendRequestDTO.getAccountId()))
                .flatMap(accountDO -> notificationService.sendAlertNotification(Collections.singletonList(notificationSendRequestDTO.getAccountId()), notificationSendRequestDTO.getData())
                        .collectList()
                        .filter(integers -> integers.stream().anyMatch(integer -> integer == 1))
                        .map(aBoolean -> ResponseDTO.builder().message("notification sent successfully").build())
                        .switchIfEmpty(Mono.error(new NotificationException())))
                .switchIfEmpty(Mono.error(new AccountNotExistException()));
    }

    @PostMapping(value = "/sendToGroup", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseDTO> sendToGroup(@RequestHeader("accountId") final String accountId,
                                  @Validated(ValidationMarker.GroupIDMandatoryMarker.class)
                                  @RequestBody NotificationSendRequestDTO notificationSendRequestDTO) {

        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .flatMap(accountDO -> groupRepository.findByGroupId(notificationSendRequestDTO.getGroupId()))
                .flatMap(groupDO -> notificationService.sendNotificationToGroup(groupDO, notificationSendRequestDTO.getData()))
                .map(aBoolean -> ResponseDTO.builder().message("notification sent successfully").build())
                .switchIfEmpty(Mono.error(new AccountNotExistException()));
    }

    @PostMapping(value = "/sendToCompany", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseDTO> sendToCompany(@RequestHeader("accountId") final String accountId,
                                   @Validated(ValidationMarker.CompanyIDMandatoryMarker.class)
                                   @RequestBody NotificationSendRequestDTO notificationSendRequestDTO) {

        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .filter(accountDo -> accountDo.getCompanyId().equals(notificationSendRequestDTO.getCompanyId()))
                .map(accountDO -> accountRepository.findByCompanyIdAndStatus(notificationSendRequestDTO.getCompanyId(), AccountStatus.ACTIVE)
                        .map(AccountDO::getAccountId)
                        .collectList()
                        .flatMapMany(accountIds -> notificationService.sendAlertNotification(accountIds, notificationSendRequestDTO.getData()))
                        .collectList()
                        .map(integers -> integers.stream().anyMatch(integer -> integer == 1))
                )
                .map(aBoolean -> ResponseDTO.builder().message("notification sent successfully").build())
                .switchIfEmpty(Mono.error(new AccountNotExistException()));
    }
}