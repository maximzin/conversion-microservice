package com.zinoviev.conversion_microservice.core.exception;

public class NotSupportedFileExtensionException extends BusinessException {
    public NotSupportedFileExtensionException(String message) {
        super(message);
    }
}
