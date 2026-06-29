package com.zinoviev.conversion_microservice.conversion.service.converterRegistry;

import com.zinoviev.conversion_microservice.conversion.api.FileConverter;

import java.util.Optional;

public interface ConverterRegistryService {

    Optional<FileConverter> getConverter(String fileExtension);

}
