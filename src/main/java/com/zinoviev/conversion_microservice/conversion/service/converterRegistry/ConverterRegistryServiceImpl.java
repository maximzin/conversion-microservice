package com.zinoviev.conversion_microservice.conversion.service.converterRegistry;

import com.zinoviev.conversion_microservice.conversion.api.FileConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConverterRegistryServiceImpl implements ConverterRegistryService {

    private final List<FileConverter> fileConverterList;

    @Override
    public Optional<FileConverter> getConverter(String fileExtension) {

        // У нас здесь должны быть собраны все бины конвертеров
        return fileConverterList
                .stream()
                .filter(fc -> fc.supports(fileExtension))
                .findFirst();
    };

}
