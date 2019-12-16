package com.cloudio.rest.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BrainTreeTokenException extends RuntimeException {
    public BrainTreeTokenException(final String message) {
        super(message);
    }
}
