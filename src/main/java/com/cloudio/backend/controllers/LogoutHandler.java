package com.cloudio.backend.controllers;

import com.cloudio.backend.model.ResponseModel;
import com.cloudio.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/logout")
@RequiredArgsConstructor
public class LogoutHandler {

    @Autowired
    AuthService authService;

    public LogoutHandler(AuthService authService) {
        this.authService = authService;
    }


    public ResponseEntity<ResponseModel> logout(@RequestHeader("Authorization") final String token) {
        authService.logout(token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
