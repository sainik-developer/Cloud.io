package com.cloudio.rest.exception;

public class InvalidTempTokenException extends RuntimeException {
    public InvalidTempTokenException(final String message) {
        super(message);
    }
}
