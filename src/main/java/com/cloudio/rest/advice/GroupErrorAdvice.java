package com.cloudio.rest.advice;

import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GroupErrorAdvice {

    @ExceptionHandler(value = NotAuthorizedToUpdateCompanyProfileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO notAuthorizedToUpdateCompanyProfileException(final Exception e) {
        return new ResponseDTO(null, "User is not admin or no more active to update the company profile", null);
    }


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
        return new ResponseDTO(HttpStatus.CONFLICT.value(), "Company name already in use", null);
    }

    @ExceptionHandler(value = SubscriptionException.class)
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    public ResponseDTO invalidNonceException(final Exception e) {
        return new ResponseDTO(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage(), null);
    }

    @ExceptionHandler(value = BrainTreeTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO brainTreeTokenException(final Exception e) {
        return new ResponseDTO(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> yourExceptionHandler(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errors;
    }
}
