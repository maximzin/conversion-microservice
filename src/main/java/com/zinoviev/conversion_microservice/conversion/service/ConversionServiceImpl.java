package com.zinoviev.conversion_microservice.conversion.service;

import com.zinoviev.conversion_microservice.common.exception.NotSupportedFileExtensionException;
import com.zinoviev.conversion_microservice.common.exception.conversion.ZipExtractionException;
import com.zinoviev.conversion_microservice.common.util.FileKeyUtils;
import com.zinoviev.conversion_microservice.conversion.api.FileConverter;
import com.zinoviev.conversion_microservice.conversion.service.converterRegistry.ConverterRegistryService;
import com.zinoviev.conversion_microservice.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class ConversionServiceImpl implements ConversionService {

    @Value("${storage.directory.conversion.processed.pdf}")
    private String dirForConvertedPdf;

    private final StorageService storageService;
    private final ConverterRegistryService converterRegistryService;

    // Типы файлов, которые считаются архивами
    private static final Set<String> ARCHIVE_EXTENSIONS = Set.of("zip");

    public ConversionServiceImpl(StorageService storageService, ConverterRegistryService converterRegistryService) {
        this.storageService = storageService;
        this.converterRegistryService = converterRegistryService;
    }

    // Получаем fileKey файла в хранилище
    @Override
    public void convertFileToPdf(String fileKey) {

        // Узнаем название и расширение файла
        String fileExtension = FileKeyUtils.parseFileExtension(fileKey);

        // Если у нас архив, то каждый файл в нём превращается в PDF (если расширение позволяет)
        if (ARCHIVE_EXTENSIONS.contains(fileExtension)) {
            processArchive(fileKey);
        } else {
            processSingleFile(fileKey);
        }
    }

    // Обработка обычного файла (TXT, PNG, JPG)
    private void processSingleFile(String fileKey) {

        String fileExtension = FileKeyUtils.parseFileExtension(fileKey);
        String fileName = FileKeyUtils.parseFileNameWithoutExtension(fileKey);

        // Скачиваем
        byte[] originalFileBytes = storageService.downloadFile(fileKey);

        // Находим конвертер
        FileConverter converter = converterRegistryService.getConverter(fileExtension)
                .orElseThrow(() -> new NotSupportedFileExtensionException(
                        String.format("Формат файла %s не поддерживается", fileName + fileExtension)));

        // Конвертируем
        byte[] convertedPdfFileBytes = converter.convert(originalFileBytes);

        // Сохраняем результат
        String convertedPdfFileKey = FileKeyUtils.createFileKey(dirForConvertedPdf, fileName, ".pdf");

        storageService.uploadFile(convertedPdfFileKey, convertedPdfFileBytes);

        log.info("Сконвертирован файл: {} -> {}", fileKey, convertedPdfFileKey);

        //TODO Опубликовать в Kafka

    }


    // Обработка архива
    private void processArchive(String fileKey) {
        byte[] archiveContent = storageService.downloadFile(fileKey);

        List<ExtractedFile> extractedFiles = extractFilesFromArchive(archiveContent);

        log.info("Архив {} содержит {} файлов", fileKey, extractedFiles.size());

        for (ExtractedFile file : extractedFiles) {
            try {
                String convertedPdfFileKey = convertInnerFile(file);
                log.info("Сконвертирован файл из архива: {} -> {}", fileKey, convertedPdfFileKey);
            } catch (Exception e) {
                log.error("Не удалось сконвертировать файл '{}' из архива '{}': {}", file.name(), fileKey, e.getMessage());
            }
        }

        // TODO сделать
    }

    private List<ExtractedFile> extractFilesFromArchive(byte[] zipContent) {
        List<ExtractedFile> files = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(
                new ByteArrayInputStream(zipContent))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] content = zis.readAllBytes();
                    files.add(new ExtractedFile(entry.getName(), content));
                }
            }
        } catch (IOException e) {
            log.error("Не удалось излвечь файлы из архива: {}", e.getMessage());
            throw new ZipExtractionException("Не удалось излвечь файл из архива");
        }

        return files;
    }

    // Конвертирует один файл из архива
    private String convertInnerFile(ExtractedFile extractedFile) {
        String innerExtension = FileKeyUtils.parseFileExtension(extractedFile.name());

        // Находим конвертер для файла внутри архива
        FileConverter converter = converterRegistryService.getConverter(innerExtension)
                .orElseThrow(() -> new NotSupportedFileExtensionException(
                        String.format("Формат файла %s не поддерживается", extractedFile.name())));

        // Конвертируем
        byte[] convertedPdfFileBytes = converter.convert(extractedFile.fileBytes());

        // Сохраняем результат
        String convertedPdfFileKey = FileKeyUtils.createFileKey(dirForConvertedPdf, extractedFile.name(), ".pdf");

        storageService.uploadFile(convertedPdfFileKey, convertedPdfFileBytes);

        return convertedPdfFileKey;
    }

    // Внутренний класс для извлеченного файла
    private record ExtractedFile(String name, byte[] fileBytes) {
    }


}
