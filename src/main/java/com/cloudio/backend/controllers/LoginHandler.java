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
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginHandler  {

    @Autowired
    AuthService authService;

    @RequestMapping(value = "/{companyId}", method = RequestMethod.POST)
    public ResponseEntity<ResponseModel> login(@PathVariable("companyId") final String companyId, @RequestHeader("Authorization") final String tempAuthTokenStr) {
        try {
            log.info("LoginHandler entering with company Id  {} ", companyId);
            Map<String, String> token = new HashMap<>();
            token.put("token", authService.login(tempAuthTokenStr, companyId) );
            ResponseModel responseModel = new ResponseModel(token, "success", ResponseModel.ResponseCode.SUCCESS);
            log.info("LoginHandler finished for company {}", companyId);
            return ResponseEntity.status(HttpStatus.OK).body(responseModel);
        } catch (final Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseModel(null, e.getMessage(),ResponseModel.ResponseCode.SERVER_ERROR));

        }
    }
}
