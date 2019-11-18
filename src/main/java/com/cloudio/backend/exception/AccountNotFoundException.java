package com.cloudio.backend.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(final String message) {
        super(message);
    }
}
