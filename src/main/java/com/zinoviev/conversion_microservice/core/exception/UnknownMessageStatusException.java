package com.zinoviev.conversion_microservice.core.exception;

public class UnknownMessageStatusException extends BusinessException {
    public UnknownMessageStatusException(String message) {
        super(message);
    }
}
