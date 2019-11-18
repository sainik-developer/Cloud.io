package com.cloudio.backend.controllers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.cloudio.backend.model.ResponseModel;
import com.cloudio.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/verify")
@RequiredArgsConstructor
public class VerifyHandler  {

    @Autowired
    AuthService authService;


    @RequestMapping(value = "/{phoneNumber}/{code}", method = RequestMethod.POST)
    public ResponseEntity<ResponseModel> verify(@PathVariable("phoneNumber") final String phoneNumber, @PathVariable("code") final String code) {
        ResponseModel responseModel = null;
        try {
            log.info("com.qring.lamda.auth.SignupHandler::handleRequest.verify entering with phone Number {} ", phoneNumber);
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("temp-authorization-token", authService.createTemporaryToken(phoneNumber, code));
            responseMap.put("companyList", authService.verify(phoneNumber, code));
            responseModel = new ResponseModel(responseMap,"success", ResponseModel.ResponseCode.SUCCESS);
            log.info("com.qring.lamda.auth.SignupHandler::handleRequest.verify finished {} ", phoneNumber);
            return ResponseEntity.status(HttpStatus.OK).body(responseModel);
        } catch (final Exception e) {
            responseModel = new ResponseModel(null, e.getMessage(),ResponseModel.ResponseCode.BAD_REQUEST);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseModel);
    }
}
