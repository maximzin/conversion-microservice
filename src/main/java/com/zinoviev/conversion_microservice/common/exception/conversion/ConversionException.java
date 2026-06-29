package com.zinoviev.conversion_microservice.common.exception.conversion;

import com.zinoviev.conversion_microservice.common.exception.ApplicationException;

public class ConversionException extends ApplicationException {

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
