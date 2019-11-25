package com.cloudio.rest.advice;

import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.exception.*;
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
        return new ResponseDTO(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);
    }

    @ExceptionHandler(value = SuspiciousStateException.class)
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    public ResponseDTO suspiciousStateException(final Exception e) {
        return new ResponseDTO(null, "unknown to developer, should not happen, either data is corrupted or manually db is modified", null);
    }

    @ExceptionHandler(value = UnautherizedToInviteException.class)
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    public ResponseDTO unautherizedToInviteException(final Exception e) {
        return new ResponseDTO(null, "You are unauthorized to invite anyone for this company", null);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseDTO invalidTokenException(final Exception e) {
        return new ResponseDTO(null, "invalid token", null);
    }

    @ExceptionHandler(value = CompanyNameNotUniqueException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseDTO companyNameNotUniqueException(final Exception e) {
        return new ResponseDTO(null, "Company name already in use", null);
    }
}
