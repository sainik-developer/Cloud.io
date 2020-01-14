package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.CompanyDTO;
import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.exception.CompanyNameNotUniqueException;
import com.cloudio.rest.exception.InvalidTempTokenException;
import com.cloudio.rest.exception.NotAuthorizedToUpdateCompanyProfileException;
import com.cloudio.rest.mapper.CompanyMapper;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.CompanyStatus;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import com.cloudio.rest.service.AWSS3Services;
import com.cloudio.rest.service.AccountService;
import com.cloudio.rest.service.AuthService;
import com.cloudio.rest.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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

    @PostMapping("")
    @ResponseStatus(value = HttpStatus.CREATED)
    public Mono<CompanyDTO> createCompany(@Validated @RequestBody CompanyDTO companyDTO,
                                          @RequestHeader("temp-authorization-token") final String authorizationToken) {
        log.info("Company going to be created with {}", companyDTO);
        return authService.isValidToken(authorizationToken)
                .flatMap(s -> companyService.isCompanyNameUnique(companyDTO.getName()))
                .map(unique -> {
                    if (!unique) {
                        throw new CompanyNameNotUniqueException();
                    }
                    return "";
                })
                .map(companyImageUrl -> {
                    companyDTO.setCompanyId("CIO:COM:" + UUID.randomUUID().toString());
                    companyDTO.setCompanyStatus(CompanyStatus.NOT_VERIFIED);
                    return companyDTO;
                })
                .map(CompanyMapper.INSTANCE::fromDTO)
                .flatMap(companyRepository::save)
                .map(CompanyMapper.INSTANCE::toDTO)
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
