package com.cloudio.rest.exception;

public class SignInException extends RuntimeException {
    public SignInException(final String message) {
        super(message);
    }
}
