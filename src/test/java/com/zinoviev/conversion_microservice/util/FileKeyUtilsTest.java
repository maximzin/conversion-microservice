package com.zinoviev.conversion_microservice.util;

import com.zinoviev.conversion_microservice.common.util.FileKeyUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileKeyUtilsTest {

    // FileName test
    @DisplayName("Даем корректный ключ и получаем корректное имя файла")
    @Test
    void parseFileNameWithoutExtension_shouldReturnCorrectName() {
        // given
        String fileKey = "/direction/files/document.txt";

        // when
        String fileName = FileKeyUtils.parseFileNameWithoutExtension(fileKey);

        // then
        assertThat(fileName)
                .isEqualTo("document");
    }

    @DisplayName("Даем некорректный ключ и получаем пустую строку")
    @Test
    void parseFileNameWithoutExtension_shouldReturnEmptyString() {
        // given
        String fileKey = "/direction/files/document/";

        // when
        String fileName = FileKeyUtils.parseFileNameWithoutExtension(fileKey);

        // then
        assertThat(fileName)
                .isEqualTo("");
    }

    // Extension test
    @DisplayName("Даем корректный ключ и получаем корректное расширение файла")
    @Test
    void parseFileExtension_shouldReturnCorrectExt() {
        // given
        String fileKey = "/direction/files/document.txt";

        // when
        String fileName = FileKeyUtils.parseFileExtension(fileKey);

        // then
        assertThat(fileName)
                .isEqualTo("txt");
    }

    @DisplayName("Даем некорректный ключ и получаем пустую строку")
    @Test
    void parseFileExtension_shouldReturnEmptyString() {
        // given
        String fileKey = "/direction/files/document/";

        // when
        String fileName = FileKeyUtils.parseFileExtension(fileKey);

        // then
        assertThat(fileName)
                .isEqualTo("");
    }

    // Creating originalFileKey
    @DisplayName("Даем корректные параметры, должен вернуть полный ключ")
    @Test
    void createFileKey_shouldReturnCorrectFileKey() {
        // given
        String directoryPath = "/files/document/";
        String fileName = "document";
        String fileExtension = ".txt";

        // when
        String fileKey = FileKeyUtils.createFileKey(directoryPath, fileName, fileExtension);

        // then
        assertThat(fileKey)
                .isEqualTo("/files/document/document.txt");
    }
}