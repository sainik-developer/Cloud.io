package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.AccountDTO;
import com.cloudio.rest.dto.FirebaseRefreshTokenDTO;
import com.cloudio.rest.dto.InviteAccountDTO;
import com.cloudio.rest.exception.AccountNotExistException;
import com.cloudio.rest.exception.AccountProfileImageNotFoundException;
import com.cloudio.rest.exception.FirebaseException;
import com.cloudio.rest.exception.UnautherizedToInviteException;
import com.cloudio.rest.mapper.AccountMapper;
import com.cloudio.rest.pojo.AccountState;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.service.AWSS3Services;
import com.cloudio.rest.service.AccountService;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.cloudio.rest.service.FirebaseService;
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
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Validated
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final AWSS3Services awss3Services;
    private final FirebaseService firebaseService;

    @GetMapping("")
    Mono<AccountDTO> getAccountDetails(@RequestHeader("accountId") final String accountId) {
        log.info("account details to be fetched is {}", accountId);
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .map(AccountMapper.INSTANCE::toDTO)
                .doOnNext(accountDto -> log.info("fetched account details is {}", accountDto))
                .switchIfEmpty(Mono.error(new AccountNotExistException()));
    }

    @PatchMapping("/update")
    Mono<AccountDTO> updateAccount(@RequestHeader("accountId") final String accountId, @RequestBody AccountDTO accountDto) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .map(accountDo -> {
                    AccountMapper.INSTANCE.update(accountDo, accountDto);
                    return accountDo;
                })
                .flatMap(accountRepository::save)
                .map(AccountMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(AccountNotExistException::new));
    }

    @PostMapping("/invite/{companyId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    Flux<AccountDTO> inviteMember(@RequestHeader("accountId") final String accountId,
                                  @PathVariable("companyId") final String companyId,
                                  @NotEmpty(message = "atleast one member is required")
                                  @RequestBody List<@Valid InviteAccountDTO> inviteAccountDtos) {
        log.info("Invitation going to be sent to accountId {} and companyId {}", accountId, companyId);
        return accountRepository.findByAccountIdAndCompanyIdAndTypeAndStatus(accountId, companyId, AccountType.ADMIN, AccountStatus.ACTIVE)
                .doOnNext(accountDo -> log.info("accountId = {} is Admin for given companyId = {} found, hence going to invite members", accountId, companyId))
                .flatMapMany(accountDo -> Flux.fromIterable(inviteAccountDtos.stream()
                        .collect(Collectors.toMap(InviteAccountDTO::getPhoneNumber, inviteAccountDTO -> inviteAccountDTO))
                        .values().stream().peek(inviteAccountDTO -> {
                            try {
                                inviteAccountDTO.setPhoneNumber(PhoneNumberUtil.getInstance().format(PhoneNumberUtil.getInstance()
                                                .parse(inviteAccountDTO.getPhoneNumber(), accountDo.getRegionCodeForCountryCode()),
                                        PhoneNumberUtil.PhoneNumberFormat.E164));
                            } catch (final NumberParseException e) {
                                log.error("error while formatting phone number with country code");
                            }
                        }).collect(Collectors.toList())))
                .flatMap(inviteAccountDto -> accountService.createAccount(companyId,
                        inviteAccountDto.getPhoneNumber(), AccountType.MEMBER, inviteAccountDto.getFirstName(), inviteAccountDto.getLastName()))
                .map(AccountMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(UnautherizedToInviteException::new));
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
                .switchIfEmpty(Mono.error(AccountNotExistException::new));
    }

    @DeleteMapping("/avatar")
    public Mono<AccountDTO> deleteProfileImage(@RequestHeader("accountId") final String accountId) {
        log.info("delete avatar is called for accountId {}", accountId);
        return accountRepository.deleteProfileUrlByAccountId(accountId, AccountStatus.ACTIVE)
                .doOnNext(accountDo -> awss3Services.deleteFilesInS3(accountDo.getProfileUrl().substring(accountDo.getProfileUrl().lastIndexOf("/") + 1)))
                .map(accountDo -> {
                    accountDo.setProfileUrl(null);
                    return accountDo;
                })
                .map(AccountMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(AccountProfileImageNotFoundException::new));
    }

    @PostMapping("/state")
    public Mono<AccountDTO> setState(@RequestHeader("accountId") final String accountId,
                                     @RequestParam(value = "state") @Pattern(regexp = "ONLINE|OFFLINE") final String state) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .flatMap(accountD -> accountRepository.updateByAccountId(accountId, AccountStatus.ACTIVE, AccountState.valueOf(state)))
                .map(AccountMapper.INSTANCE::toDTO);
    }

    @GetMapping("/refreshFirebaseToken")
    public Mono<FirebaseRefreshTokenDTO> refreshFirebaseTokne(@RequestHeader("accountId") final String accountId) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .map(accountDo -> firebaseService.refreshFireBaseCustomToken(accountDo.getAccountId()))
                .map(s -> FirebaseRefreshTokenDTO.builder().customToken(s).build())
                .switchIfEmpty(Mono.error(FirebaseException::new));
    }

    @DeleteMapping("/revokeFirebaseToken")
    public Mono<Boolean> revokeFirebaseToken(@RequestHeader("accountId") final String accountId) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .map(accountDo -> firebaseService.revokeFireBaseCustomToken(accountDo.getAccountId()))
                .switchIfEmpty(Mono.error(FirebaseException::new));
    }
}