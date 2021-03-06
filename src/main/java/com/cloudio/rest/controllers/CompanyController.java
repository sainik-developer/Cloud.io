package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.*;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.exception.*;
import com.cloudio.rest.mapper.AccountMapper;
import com.cloudio.rest.mapper.CompanyMapper;
import com.cloudio.rest.mapper.CompanySettingMapper;
import com.cloudio.rest.mapper.GroupMapper;
import com.cloudio.rest.pojo.AccountState;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.GroupType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import com.cloudio.rest.repository.GroupRepository;
import com.cloudio.rest.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Log4j2
@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {
    private final AuthService authService;
    private final GroupService groupService;
    private final AWSS3Services awss3Services;
    private final AccountService accountService;
    private final CompanyService companyService;
    private final GroupRepository groupRepository;
    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;

    @GetMapping("/details")
    public Mono<CompanyDTO> getCompanyByAccountId(@RequestHeader("accountId") final String accountId) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .map(AccountDO::getCompanyId)
                .flatMap(companyRepository::findByCompanyId)
                .flatMap(companyDo -> groupRepository.findByCompanyId(companyDo.getCompanyId())
                        .flatMap(groupDo -> {
                            if (groupDo.getGroupType() == GroupType.DEFAULT) {
                                return accountRepository.findByCompanyIdAndStatus(groupDo.getCompanyId(), AccountStatus.ACTIVE)
                                        .map(AccountMapper.INSTANCE::toDTO)
                                        .collectList()
                                        .map(accountDtos -> {
                                            final GroupDTO groupDto = GroupMapper.INSTANCE.toDTO(groupDo);
                                            groupDto.setMembers(accountDtos);
                                            groupDto.setGroupState(groupService.getGroupStatus(accountDtos));
                                            return groupDto;
                                        })

                                        .switchIfEmpty(Mono.just(GroupMapper.INSTANCE.toDTO(groupDo)));
                            } else {
                                return accountRepository.findByAccountIdsAndStatus(groupDo.getMembers(), AccountStatus.ACTIVE)
                                        .map(AccountMapper.INSTANCE::toDTO)
                                        .collectList()
                                        .map(accountDtos -> {
                                            final GroupDTO groupDto = GroupMapper.INSTANCE.toDTO(groupDo);
                                            groupDto.setMembers(accountDtos);
                                            groupDto.setGroupState(groupService.getGroupStatus(accountDtos));
                                            return groupDto;
                                        })
                                        .switchIfEmpty(Mono.just(GroupMapper.INSTANCE.toDTO(groupDo)));
                            }
                        })
                        .collectList()
                        .map(groupDtos -> {
                            final CompanyDTO companyDto = CompanyMapper.INSTANCE.toDTO(companyDo);
                            companyDto.setGroups(groupDtos);
                            return companyDto;
                        }));
    }

    @GetMapping("/groups")
    public Flux<GroupDTO> groups(@RequestHeader("accountId") final String accountId) {
        return accountRepository.findByAccountId(accountId)
                .doOnNext(accountDo -> log.info("Group is retrieving for {}", accountDo.getCompanyId()))
                .flatMapMany(accountDo -> groupRepository.findByCompanyId(accountDo.getCompanyId()))
                .flatMap(groupDo -> {
                    if (groupDo.getGroupType() == GroupType.DEFAULT) {
                        return accountRepository.findByCompanyIdAndStatus(groupDo.getCompanyId(), AccountStatus.ACTIVE)
                                .map(AccountMapper.INSTANCE::toDTO)
                                .collectList()
                                .map(accountDtos -> {
                                    final GroupDTO groupDto = GroupMapper.INSTANCE.toDTO(groupDo);
                                    groupDto.setMembers(accountDtos);
                                    groupDto.setGroupState(groupService.getGroupStatus(accountDtos));
                                    return groupDto;
                                })
                                .switchIfEmpty(Mono.just(GroupMapper.INSTANCE.toDTO(groupDo)));
                    } else {
                        return accountRepository.findByAccountIdsAndStatus(groupDo.getMembers(), AccountStatus.ACTIVE)
                                .map(AccountMapper.INSTANCE::toDTO)
                                .collectList()
                                .map(accountDtos -> {
                                    final GroupDTO groupDto = GroupMapper.INSTANCE.toDTO(groupDo);
                                    groupDto.setMembers(accountDtos);
                                    groupDto.setGroupState(groupService.getGroupStatus(accountDtos));
                                    return groupDto;
                                })
                                .switchIfEmpty(Mono.just(GroupMapper.INSTANCE.toDTO(groupDo)));
                    }
                })
                .switchIfEmpty(Mono.error(AccountNotExistException::new));
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public Mono<CompanyDTO> createCompany(@Validated @RequestBody CompanyDTO companyDTO,
                                          @RequestHeader("temp-authorization-token") final String authorizationToken) {
        log.info("Company going to be created with {}", companyDTO);
        return authService.isValidToken(authorizationToken)
                .flatMap(companyName -> companyService.isCompanyNameUnique(companyDTO.getName()))
                .map(unique -> {
                    if (!unique) {
                        throw new CompanyNameNotUniqueException();
                    }
                    return "";
                })
                .flatMap(noStr -> companyService.createCompany(companyDTO))
                .map(CompanyMapper.INSTANCE::fromDTO)
                .flatMap(companyRepository::save)
                .map(CompanyMapper.INSTANCE::toDTO)
                .flatMap(companyDto -> accountService.createAccount(companyDto.getCompanyId(),
                        authService.decodeTempAuthToken(authorizationToken).getPhoneNumber(),
                        AccountType.ADMIN, null, null)
                        .flatMap(accountDo -> groupService.createDefaultGroup(companyDto.getCompanyId())
                                .doOnNext(groupDo -> log.info("Default group is created and details are {}", groupDo))
                                .map(GroupMapper.INSTANCE::toDTO)
                                .map(groupDto -> {
                                    groupDto.setMembers(Collections.singletonList(AccountMapper.INSTANCE.toDTO(accountDo)));
                                    companyDto.setGroups(Collections.singletonList(groupDto));
                                    return companyDto;
                                })))
                .switchIfEmpty(Mono.error(new InvalidTempTokenException("Temp token is invalid")));
    }

    @PatchMapping("")
    public Mono<ResponseDTO> addCompanyImage(@RequestHeader("temp-authorization-token") final String authorizationToken,
                                             @RequestPart(value = "image") Mono<FilePart> file) {
        return authService.isValidToken(authorizationToken)
                .flatMap(companyDo -> awss3Services.uploadFileInS3(file))
                .map(imageUrl -> ResponseDTO.builder().data(imageUrl).build())
                .switchIfEmpty(Mono.error(new InvalidTempTokenException("Temp token is invalid")));
    }

    @PostMapping("/update/avatar")
    public Mono<CompanyDTO> updateCompanyImage(@RequestHeader("accountId") final String accountId, @RequestParam("companyId") final String companyId,
                                               @RequestPart(value = "image") Mono<FilePart> file) {
        return accountRepository.findByAccountIdAndCompanyIdAndTypeAndStatus(accountId, companyId, AccountType.ADMIN, AccountStatus.ACTIVE)
                .flatMap(accountDo -> companyRepository.findByCompanyId(companyId))
                .map(companyDo -> {
                    if (companyDo.getCompanyAvatarUrl() != null) {
                        awss3Services.deleteFilesInS3(companyDo.getCompanyAvatarUrl().substring(companyDo.getCompanyAvatarUrl().lastIndexOf("/") + 1));
                    }
                    return companyDo;
                })
                .flatMap(companyDo -> awss3Services.uploadFileInS3(file)
                        .map(imageUrl -> {
                            companyDo.setCompanyAvatarUrl(imageUrl);
                            return companyDo;
                        }))
                .flatMap(companyRepository::save)
                .map(CompanyMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(NotAuthorizedToUpdateCompanyProfileException::new));
    }

    @DeleteMapping("/delete/avatar")
    public Mono<CompanyDTO> deleteCompanyImage(@RequestHeader("accountId") final String accountId, @RequestParam("companyId") final String companyId) {
        log.info("delete avatar is called for accountId {} for companyId {}", accountId, companyId);
        return accountRepository.findByAccountIdAndCompanyIdAndTypeAndStatus(accountId, companyId, AccountType.ADMIN, AccountStatus.ACTIVE)
                .flatMap(accountDo -> companyRepository.deleteProfileUrlByAccountId(companyId))
                .map(companyDo -> {
                    companyDo.setCompanyAvatarUrl(null);
                    return companyDo;
                })
                .map(CompanyMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(NotAuthorizedToUpdateCompanyProfileException::new));
    }

    @GetMapping("/members")
    public Flux<AccountDTO> getAllMembersByCompany(@RequestHeader("accountId") final String accountId, @RequestParam(value = "state", defaultValue = "", required = false) final String state) {
        return accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE)
                .map(AccountDO::getCompanyId)
                .flatMapMany(companyId -> !state.isEmpty() ? accountRepository.findByCompanyIdAndStatusAndState(companyId, AccountStatus.ACTIVE, AccountState.valueOf(state)) :
                        accountRepository.findByCompanyIdAndStatus(companyId, AccountStatus.ACTIVE))
                .map(AccountMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(AccountNotExistException::new));
    }

    @PostMapping(value = "/setting", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CompanyDTO> saveTheSetting(@RequestHeader("accountId") final String accountId, @Validated @RequestBody CompanySettingDTO companySettingDTO) {
        return accountRepository.findByAccountIdAndStatusAndType(accountId, AccountStatus.ACTIVE, AccountType.ADMIN)
                .map(AccountDO::getCompanyId)
                .flatMap(companyId -> companyService.checkIfAccountPartOfCompany(companyId, companySettingDTO)
                        .flatMap(companySettingDto -> companyRepository.findByCompanyId(companyId))
                        .map(companyDo -> {
                            companyDo.setCompanySetting(CompanySettingMapper.INSTANCE.fromDTO(companySettingDTO));
                            return companyDo;
                        })
                        .flatMap(companyRepository::save)
                        .map(CompanyMapper.INSTANCE::toDTO))
                .switchIfEmpty(Mono.error(UnAuthorizedToUpdateSettingException::new));
    }
}