package com.cloudio.backend.controllers;

import com.cloudio.backend.model.ResponseModel;
import com.cloudio.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/signup")
@RequiredArgsConstructor
@Log4j2
public class SignupHandler  {

    @Autowired
    AuthService authService;

    @RequestMapping(value = "/{phoneNumber}", method = RequestMethod.POST)
    public ResponseEntity<ResponseModel> signup(@PathVariable("phoneNumber") final String phoneNumber) {
        ResponseModel responseModel = null;
        try {
            log.info("com.qring.lamda.auth.SignupHandler::handleRequest.signup entering with phone Number {} ", phoneNumber);
            responseModel = new ResponseModel(null, authService.signup(phoneNumber),ResponseModel.ResponseCode.SUCCESS);
            log.info("com.qring.lamda.auth.SignupHandler::handleRequest.signup finished for number {}", phoneNumber);
            return ResponseEntity.status(HttpStatus.OK).body(responseModel);

        } catch (final Exception e) {
            e.printStackTrace();

            responseModel = new ResponseModel(null, "Sign in failed. please contact the cloud.io team. "+e.getMessage(),ResponseModel.ResponseCode.BAD_REQUEST);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseModel);
    }
}
