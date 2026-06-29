package com.zinoviev.conversion_microservice.common.exception;

public class NotSupportedFileExtensionException extends BusinessException {
    public NotSupportedFileExtensionException(String message) {
        super(message);
    }
}
