package com.zinoviev.conversion_microservice.core.exception;

public class BusinessException extends ApplicationException {
    public BusinessException(String message) {
        super(message);
    }
}