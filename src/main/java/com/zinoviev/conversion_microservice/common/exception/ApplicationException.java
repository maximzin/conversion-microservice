package com.zinoviev.conversion_microservice.common.exception;

public class ApplicationException extends RuntimeException {
    private final String message;
    protected ApplicationException(String message) {
        super(message);
        this.message = message;
    }
}
