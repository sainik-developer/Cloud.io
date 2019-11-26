package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.CompanyDTO;
import com.cloudio.rest.dto.LoginResponseDTO;
import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.dto.VerifyResponseDTO;
import com.cloudio.rest.exception.InvalidTokenException;
import com.cloudio.rest.exception.VerificationException;
import com.cloudio.rest.mapper.CompanyMapper;
import com.cloudio.rest.service.AuthService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiResponses(value = {
            @ApiResponse(code = 200, response = ResponseDTO.class, message = "sms related message, and one sms is sent to supplied phone number")
    })
    @GetMapping("/signup/{phoneNumber}")
    public Mono<ResponseDTO> signUp(@PathVariable("phoneNumber") final String phoneNumber) {
        log.info("cloud io signup entering with phone Number {} ", phoneNumber);
        return authService.signup(phoneNumber)
                .map(s -> ResponseDTO.builder().message(s).build());
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, response = VerifyResponseDTO.class, responseContainer = "List", message = "Account is updated successfully"),
            @ApiResponse(code = 401, response = String.class, message = "Phone number is not found or code is not matched")
    })
    @PostMapping("/verify/{phoneNumber}/{code}")
    public Mono<VerifyResponseDTO> verify(@PathVariable("phoneNumber") final String phoneNumber,
                                          @PathVariable("code") final String code) {
        log.info("cloud io verify entering with phone Number {} ", phoneNumber);
        return authService.verify(phoneNumber, code)
                .filter(Boolean::booleanValue)
                .doOnNext(aBoolean -> log.info("verification status {}", aBoolean))
                .flatMap(aBoolean -> authService.retrieveAllAssociatedCompanyDetails(phoneNumber)
                        .map(CompanyMapper.INSTANCE::toDTO)
                        .collectList())
                .map(companyDtos -> VerifyResponseDTO.builder()
                        .companies(companyDtos)
                        .token(authService.createTemporaryToken(phoneNumber, code))
                        .build()
                )
                .doOnNext(listResponseEntity -> log.info("verify finished {}", phoneNumber))
                .switchIfEmpty(Mono.error(new VerificationException("Phone number is not found or code is not matched")));
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, response = String.class, message = "Authorization as header containing token"),
            @ApiResponse(code = 401, response = String.class, message = "Phone number is not found or code is not matched")

    })
    @PostMapping("/login/{companyId}")
    public Mono<LoginResponseDTO> login(@PathVariable("companyId") final String companyId,
                                        @RequestHeader("temp-authorization-token") final String tempAuthToken) {
        log.info("LoginHandler entering with company Id  {} with and temp-authorization-token {}", companyId, tempAuthToken);
        return authService.login(tempAuthToken, companyId)
                .map(s -> LoginResponseDTO.builder().authorization(s).build())
                .switchIfEmpty(Mono.error(new VerificationException("Phone number is not found or code is not matched")));
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, response = CompanyDTO.class, message = "Logged out successfully"),
            @ApiResponse(code = 406, response = CompanyDTO.class, message = "invalid token")
    })
    @PostMapping("/logout")
    public Mono<String> logout(@RequestHeader("Authorization") final String token) {
        return authService.logout(token).switchIfEmpty(Mono.error(new InvalidTokenException()));
    }
}
