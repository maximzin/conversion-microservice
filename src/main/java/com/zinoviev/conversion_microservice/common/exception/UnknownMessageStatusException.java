package com.zinoviev.conversion_microservice.common.exception;

public class UnknownMessageStatusException extends BusinessException {
    public UnknownMessageStatusException(String message) {
        super(message);
    }
}
