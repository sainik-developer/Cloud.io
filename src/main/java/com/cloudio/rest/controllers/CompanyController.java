package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.CompanyDTO;
import com.cloudio.rest.dto.GroupDTO;
import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.exception.CompanyNameNotUniqueException;
import com.cloudio.rest.exception.GroupNotFoundException;
import com.cloudio.rest.exception.InvalidTempTokenException;
import com.cloudio.rest.exception.NotAuthorizedToUpdateCompanyProfileException;
import com.cloudio.rest.mapper.CompanyMapper;
import com.cloudio.rest.mapper.GroupMapper;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.CompanyStatus;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import com.cloudio.rest.repository.GroupRepository;
import com.cloudio.rest.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final AuthService authService;
    private final CompanyRepository companyRepository;
    private final AccountService accountService;
    private final CompanyService companyService;
    private final AccountRepository accountRepository;
    private final AWSS3Services awss3Services;
    private final GroupService groupService;
    private final GroupRepository groupRepository;


    @GetMapping("/group")
    public Flux<GroupDTO> groups(@RequestHeader("accountId") final String accountId) {
        return accountRepository.findByAccountId(accountId)
                .doOnNext(accountDo -> log.info("Group is retrieving for {}", accountDo.getCompanyId()))
                .flatMapMany(accountDo -> groupRepository.findByCompanyId(accountDo.getCompanyId()))
                .map(GroupMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(new GroupNotFoundException("There is no group for given company id")));
    }


    @PostMapping("")
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
                .map(noStr -> {
                    companyDTO.setCompanyId("CIO:COM:" + UUID.randomUUID().toString());
                    companyDTO.setCompanyStatus(CompanyStatus.NOT_VERIFIED);
                    return companyDTO;
                })
                .map(CompanyMapper.INSTANCE::fromDTO)
                .flatMap(companyRepository::save)
                .map(CompanyMapper.INSTANCE::toDTO)
                .flatMap(companyDto -> groupService.createDefaultGroup(companyDto.getCompanyId())
                        .doOnNext(groupDo -> log.info("Default group is created and details are {}", groupDo))
                        .map(GroupMapper.INSTANCE::toDTO)
                        .map(groupDto -> {
                            companyDto.setGroups(Collections.singletonList(groupDto));
                            return companyDto;
                        })
                )
                .flatMap(companyDto -> accountService.createAccount(companyDto.getCompanyId(), authService.decodeTempAuthToken(authorizationToken).getPhoneNumber(), AccountType.ADMIN, null, null)
                        .map(accountDto -> companyDto))
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
                .switchIfEmpty(Mono.error(new NotAuthorizedToUpdateCompanyProfileException()));
    }

}