package com.cloudio.backend.controllers;

import com.cloudio.backend.dto.CompanyDTO;
import com.cloudio.backend.dto.ResponseDTO;
import com.cloudio.backend.exception.VerificationException;
import com.cloudio.backend.mapper.CompanyMapper;
import com.cloudio.backend.service.AuthService;
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

    @GetMapping("/signup/{phoneNumber}")
    public Mono<ResponseDTO> signUp(@PathVariable("phoneNumber") final String phoneNumber) {
        log.info("cloud io signup entering with phone Number {} ", phoneNumber);
        return authService.signup(phoneNumber)
                .map(s -> ResponseDTO.builder().message(s).build());
    }

    @PostMapping("/verify/{phoneNumber}/{code}")
    public Mono<ResponseEntity<List<CompanyDTO>>> verify(@PathVariable("phoneNumber") final String phoneNumber,
                                                         @PathVariable("code") final String code) {
        log.info("cloud io verify entering with phone Number {} ", phoneNumber);
        return authService.verify(phoneNumber, code)
                .map(CompanyMapper.INSTANCE::toDTO)
                .collectList()
                .map(companyDos -> ResponseEntity.ok()
                        .header("temp-authorization-token", authService.createTemporaryToken(phoneNumber, code))
                        .body(companyDos)
                )
                .doOnNext(listResponseEntity -> log.info("verify finished {}", phoneNumber));
    }

    @PostMapping("/login/{companyId}")
    public Mono<ResponseEntity<String>> login(@PathVariable("companyId") final String companyId,
                                              @RequestHeader("temp-authorization-token") final String tempAuthToken) {
        log.info("LoginHandler entering with company Id  {} with and temp-authorization-token {}", companyId, tempAuthToken);
        return authService.login(tempAuthToken, companyId)
                .map(s -> ResponseEntity.ok()
                        .header("Authorization", s).body(""))
                .switchIfEmpty(Mono.error(new VerificationException("Phone number is not found or code is not matched")));
    }

    @PostMapping("/logout")
    public Mono<String> logout(@RequestHeader("Authorization") final String token) {
        return authService.logout(token);
    }
}
