package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.NotificationSendRequestDTO;
import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.exception.AccountNotExistException;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.GroupType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import com.cloudio.rest.repository.GroupRepository;
import com.cloudio.rest.repository.TokenRepository;
import com.cloudio.rest.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Log4j2
@Validated
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;
    private final GroupRepository groupRepository;
    private final TokenRepository tokenRepository;
    private final NotificationService notificationService;

  /*  @PostMapping("/sendToAccount")
    Mono<ResponseDTO> sendToAccount(@RequestHeader("accountId") final String accountId, @RequestBody final NotificationSendRequestDTO notificationSendRequestDTO) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .flatMap(accountDO -> tokenRepository.findByAccountId(notificationSendRequestDTO.getAccountId())
                        .flatMap(tokenDO -> notificationService.sendNotification(tokenDO.getDevice(), tokenDO.getToken(), notificationSendRequestDTO.getData()))
                        .filter(Boolean::booleanValue)
                        .map(aBoolean -> ResponseDTO.builder().message("notification sent...").build())
                        .switchIfEmpty(Mono.error(new AccountNotExistException())));
    }*/

    @PostMapping("/sendToAccount")
    Mono<ResponseDTO> sendToAccount(@RequestHeader("accountId") final String accountId, @RequestBody final NotificationSendRequestDTO notificationSendRequestDTO) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .flatMap(accountDO -> tokenRepository.findByAccountId(notificationSendRequestDTO.getAccountId())
                        .flatMap(tokenDO -> notificationService.sendNotificationToAccount(tokenDO, notificationSendRequestDTO.getData()))
                        .filter(Boolean::booleanValue)
                        .map(aBoolean -> ResponseDTO.builder().message("notification sent...").build())
                        .switchIfEmpty(Mono.error(new AccountNotExistException())));
    }

    @PostMapping("/sendToGroup")
    Mono<ResponseDTO> sendToGroup(@RequestHeader("accountId") final String accountId, @RequestBody NotificationSendRequestDTO notificationSendRequestDTO) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .flatMap(accountDO -> groupRepository.findByGroupId(notificationSendRequestDTO.getGroupId()))
                .flatMap(groupDO -> notificationService.sendNotificationToGroup(groupDO,notificationSendRequestDTO.getData()))
                .filter(Boolean::booleanValue)
                .map(aBoolean -> ResponseDTO.builder().message("Notification sent to all group members...").build())
                .switchIfEmpty(Mono.error(new AccountNotExistException()));
    }

}