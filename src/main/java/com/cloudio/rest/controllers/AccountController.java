package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.AccountDTO;
import com.cloudio.rest.dto.InviteAccountDTO;
import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.exception.AccountNotExistException;
import com.cloudio.rest.exception.UnautherizedToInviteException;
import com.cloudio.rest.mapper.AccountMapper;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.service.AccountService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @ApiResponses(value = {
            @ApiResponse(code = 200, response = AccountDTO.class, message = "Account is fetched"),
            @ApiResponse(code = 404, response = ResponseDTO.class, message = "No active account found"),
    })
    @GetMapping("")
    Mono<AccountDTO> getAccountDetails(@RequestHeader("accountId") final String accountId) {
        log.info("account details to be fetched is {}", accountId);
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .map(AccountMapper.INSTANCE::toDTO)
                .doOnNext(accountDto -> log.info("fetched account details is {}", accountDto))
                .switchIfEmpty(Mono.error(new AccountNotExistException()));
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, response = AccountDTO.class, message = "Account is updated successfully"),
            @ApiResponse(code = 404, response = ResponseDTO.class, message = "No active account found"),
            @ApiResponse(code = 401, response = ResponseDTO.class, message = "user is unauthorized to access the system")
    })
    @PatchMapping("/update")
    Mono<AccountDTO> updateAccount(@RequestHeader("accountId") final String accountId, @Validated @RequestBody AccountDTO accountDto) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .map(accountDo -> {
                    AccountMapper.INSTANCE.update(accountDo, accountDto);
                    return accountDo;
                })
                .flatMap(accountRepository::save)
                .map(AccountMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(new AccountNotExistException()));
    }

    @ApiResponses(value = {
            @ApiResponse(code = 201, response = AccountDTO.class, responseContainer = "List", message = "The list of invited members"),
            @ApiResponse(code = 417, response = ResponseDTO.class, message = "User does not have permission to invite someone"),
            @ApiResponse(code = 401, response = ResponseDTO.class, message = "user is unauthorized to access the system")
    })
    @PostMapping("/invite/{companyId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    Flux<AccountDTO> inviteMember(@RequestHeader("accountId") final String accountId,
                                  @PathVariable("companyId") final String companyId,
                                  @Validated @RequestBody List<InviteAccountDTO> inviteAccountDtos) {
        log.info("Invitation going to be sent to accountId {} and companyId {}", accountId, companyId);
        return accountRepository.findByAccountIdAndCompanyIdAndType(accountId, companyId, AccountType.ADMIN)
                .doOnNext(accountDo -> log.info("accountid = {} is Admin for given companyid = {} found, hence going to invite members", accountId, companyId))
                .flatMapMany(accountDo -> Flux.fromIterable(inviteAccountDtos))
                .flatMap(inviteAccountDto -> accountService.createAccount(companyId,
                        inviteAccountDto.getPhoneNumber(),
                        AccountType.MEMBER,
                        inviteAccountDto.getFirstName(),
                        inviteAccountDto.getLastName()))
                .switchIfEmpty(Mono.error(new UnautherizedToInviteException()));
    }
}
