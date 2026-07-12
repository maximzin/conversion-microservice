package com.zinoviev.conversion_microservice.core.exception.conversion;

import com.zinoviev.conversion_microservice.core.exception.ApplicationException;

public class ConversionException extends ApplicationException {

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
