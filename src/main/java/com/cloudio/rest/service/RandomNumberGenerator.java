package com.cloudio.rest.service;


import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class RandomNumberGenerator {

    public String generateRandomNumberOfLength(int length) {
        return String.valueOf((int) Math.floor(1000 + Math.random() * 9000));
    }
}
