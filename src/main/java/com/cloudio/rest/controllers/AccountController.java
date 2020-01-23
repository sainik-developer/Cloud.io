package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.AccountDTO;
import com.cloudio.rest.dto.InviteAccountDTO;
import com.cloudio.rest.exception.AccountNotExistException;
import com.cloudio.rest.exception.UnautherizedToInviteException;
import com.cloudio.rest.mapper.AccountMapper;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.service.AWSS3Services;
import com.cloudio.rest.service.AccountService;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Log4j2
@Validated
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final AWSS3Services awss3Services;

    private String countryCode;
    //private int len;

    @GetMapping("")
    Mono<AccountDTO> getAccountDetails(@RequestHeader("accountId") final String accountId) {
        log.info("account details to be fetched is {}", accountId);
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .map(AccountMapper.INSTANCE::toDTO)
                .doOnNext(accountDto -> log.info("fetched account details is {}", accountDto))
                .switchIfEmpty(Mono.error(new AccountNotExistException()));
    }

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

    @PostMapping("/invite/{companyId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    Flux<AccountDTO> inviteMember(@RequestHeader("accountId") final String accountId,
                                  @PathVariable("companyId") final String companyId,
                                  @NotEmpty(message = "atleast one member is required")
                                  @RequestBody List<@Valid InviteAccountDTO> inviteAccountDtos) {
        log.info("Invitation going to be sent to accountId {} and companyId {}", accountId, companyId);
        return accountRepository.findByAccountIdAndCompanyIdAndTypeAndStatus(accountId, companyId, AccountType.ADMIN, AccountStatus.ACTIVE)
                .doOnNext(accountDo -> {log.info("accountid = {} is Admin for given companyid = {} found, hence going to invite members", accountId, companyId);
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    String phone=accountDo.getPhoneNumber();
                    try
                    {
                        Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phone, "");
                        countryCode = "+"+numberProto.getCountryCode();              // phone must begin with '+'
                    }
                    catch (NumberParseException e) {System.err.println("NumberParseException was thrown: " + e.toString());}    //this line never gonna execute
                })
                .flatMapMany(accountDo -> Flux.fromIterable(inviteAccountDtos))
                .flatMap(inviteAccountDto ->
                        {
                            String phoneNumber=inviteAccountDto.getPhoneNumber();
                            if(!phoneNumber.contains("+"))
                                    phoneNumber=countryCode+phoneNumber;
                            return accountService.createAccount(companyId,phoneNumber,AccountType.MEMBER,inviteAccountDto.getFirstName(),inviteAccountDto.getLastName());
                        })
                .switchIfEmpty(Mono.error(new UnautherizedToInviteException()));
    }

    @PostMapping("/avatar")
    public Mono<AccountDTO> uploadProfileImage(@RequestHeader("accountId") final String accountId,
                                               @RequestPart(value = "image") Mono<FilePart> file) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .map(accountDo -> {
                    if (accountDo.getProfileUrl() != null) {
                        awss3Services.deleteFilesInS3(accountDo.getProfileUrl().substring(accountDo.getProfileUrl().lastIndexOf("/") + 1));
                    }
                    return accountDo;
                })
                .flatMap(accountDo -> awss3Services.uploadFileInS3(file)
                        .map(imageUrl -> {
                            accountDo.setProfileUrl(imageUrl);
                            return accountDo;
                        }))
                .flatMap(accountRepository::save)
                .map(AccountMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(new AccountNotExistException()));
    }
}