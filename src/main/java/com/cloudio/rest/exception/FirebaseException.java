package com.cloudio.rest.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FirebaseException extends RuntimeException {
    public FirebaseException(final String message) {
        super(message);
    }

}
