package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.CompanyDTO;
import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.exception.CompanyNameNotUniqueException;
import com.cloudio.rest.exception.InvalidTempTokenException;
import com.cloudio.rest.mapper.CompanyMapper;
import com.cloudio.rest.pojo.AccountType;
import com.cloudio.rest.pojo.CompanyStatus;
import com.cloudio.rest.repository.CompanyRepository;
import com.cloudio.rest.service.AWSS3Services;
import com.cloudio.rest.service.AccountService;
import com.cloudio.rest.service.AuthService;
import com.cloudio.rest.service.CompanyService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
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

    @ApiResponses(value = {
            @ApiResponse(code = 201, response = CompanyDTO.class, message = "Company is created by admin"),
            @ApiResponse(code = 401, response = ResponseDTO.class, message = "Temp token is invalid hence company could not be created")
    })
    @PostMapping("")
    @ResponseStatus(value = HttpStatus.CREATED)
    public Mono<CompanyDTO> createCompany(@Validated @RequestBody CompanyDTO companyDTO,
                                          @RequestHeader("temp-authorization-token") final String authorizationToken) {
        log.info("Company going to be created with ");
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
                    companyDTO.setCompanyAvatarUrl(companyImageUrl);
                    companyDTO.setCompanyStatus(CompanyStatus.NOT_VERIFIED);
                    return companyDTO;
                })
                .doOnNext(companyDto -> accountService.createAccount(companyDto.getCompanyId(), authService.decodeTempAuthToken(authorizationToken).getPhoneNumber(), AccountType.ADMIN, null, null).subscribe())
                .map(CompanyMapper.INSTANCE::fromDTO)
                .flatMap(companyRepository::save)
                .map(CompanyMapper.INSTANCE::toDTO)
                .switchIfEmpty(Mono.error(new InvalidTempTokenException("Temp token is invalid")));
    }
}
