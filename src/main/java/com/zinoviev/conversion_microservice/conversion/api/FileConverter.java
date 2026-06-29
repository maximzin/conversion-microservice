package com.zinoviev.conversion_microservice.conversion.api;

public interface FileConverter {

    boolean supports(String fileExtension);

    byte[] convert(String fileExtension, byte[] bytes);

}
