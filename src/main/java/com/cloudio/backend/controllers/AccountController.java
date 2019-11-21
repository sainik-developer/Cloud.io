package com.cloudio.backend.controllers;

import com.cloudio.backend.dto.AccountDTO;
import com.cloudio.backend.dto.InviteAccountDTO;
import com.cloudio.backend.exception.AccountNotExistException;
import com.cloudio.backend.mapper.AccountMapper;
import com.cloudio.backend.pojo.AccountStatus;
import com.cloudio.backend.pojo.AccountType;
import com.cloudio.backend.repository.AccountRepository;
import com.cloudio.backend.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

//    @PostMapping("/invite/{companyId}")
//    Flux<AccountDTO> inviteMember(@RequestHeader("accountId") final String accountId,
//                                  @PathVariable("companyId") final String companyId,
//                                  @Validated @RequestBody List<InviteAccountDTO> inviteAccountDtos) {
//        accountRepository.findByAccountIdAndCompanyIdAndType(accountId, companyId, AccountType.ADMIN)
//                .flatMapMany(accountDo ->Flux.fromIterable(inviteAccountDtos))
//                .switchIfEmpty()
//    }
}
