package com.cloudio.backend.advice;

import com.cloudio.backend.dto.ResponseDTO;
import com.cloudio.backend.exception.AccountNotExistException;
import com.cloudio.backend.exception.InvalidTempTokenException;
import com.cloudio.backend.exception.SignInException;
import com.cloudio.backend.exception.VerificationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GroupErrorAdvice {

    @ExceptionHandler(value = SignInException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO signInException(final Exception e) {
        return new ResponseDTO(null, e.getMessage(), null);
    }

    @ExceptionHandler(value = VerificationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseDTO verificationException(final Exception e) {
        return new ResponseDTO(null, e.getMessage(), null);
    }

    @ExceptionHandler(value = AccountNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO accountNotExistException(final Exception e) {
        return new ResponseDTO(HttpStatus.NOT_FOUND.value(), "Account not found", null);
    }

    @ExceptionHandler(value = InvalidTempTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseDTO invalidTempTokenException(final Exception e) {
        return new ResponseDTO(HttpStatus.NOT_FOUND.value(), "Temp token is invalid", null);
    }
}
