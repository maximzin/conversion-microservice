package com.zinoviev.conversion_microservice.conversion.service;

import java.util.List;

public interface ConversionService {

    List<String> convertFileToPdf(String fileKey);

}
