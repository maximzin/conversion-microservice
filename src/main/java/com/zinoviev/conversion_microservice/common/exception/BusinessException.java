package com.zinoviev.conversion_microservice.common.exception;

public class BusinessException extends ApplicationException {
    public BusinessException(String message) {
        super(message);
    }
}