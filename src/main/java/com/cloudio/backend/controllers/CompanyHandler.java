package com.cloudio.backend.controllers;

import com.cloudio.backend.model.Company;
import com.cloudio.backend.model.CompanyStatus;
import com.cloudio.backend.model.ResponseModel;
import com.cloudio.backend.service.AuthService;
import com.cloudio.backend.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyHandler  {
    @Autowired
    AuthService authService;

    @Autowired
    CompanyService companyService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<ResponseModel> createCompany(Map<String, Object>dataMap,
                                                       @RequestHeader("Authorization") final String authKey) {


        if (authKey == null || !authService.isValidToken(authKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseModel(null,"forbidden",ResponseModel.ResponseCode.FORBIDDEN ));
        }

        if(dataMap == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseModel(null,"null input data",ResponseModel.ResponseCode.BAD_REQUEST ));

        }
        Company company = new Company();
        company.setName((String) dataMap.get("name"));
        company.setCompanyAvatarUrl((String) dataMap.get("profileUrl"));
        company.setCompanyId(UUID.randomUUID().toString());
        company.setCompanyStatus(CompanyStatus.NOT_VERIFIED); // (?)
        company = companyService.createCompany(company);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseModel(company,"Success",ResponseModel.ResponseCode.SUCCESS ));


    }



}
