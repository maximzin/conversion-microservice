package com.zinoviev.conversion_microservice.conversion.txt;

import lombok.SneakyThrows;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TxtFileConverterTest {

    private final TxtFileConverter converter = new TxtFileConverter();

    @DisplayName("Конвертирует TXT файл в PDF")
    @SneakyThrows
    @Test
    void convert_shouldConvertTxtToPdf() {
        // given
        String text = "Title text\nBody text";
        byte[] input = text.getBytes(StandardCharsets.UTF_8);
        String fileExtension = "txt";

        // when
        byte[] pdfBytes = converter.convert(fileExtension,input);

        // then
        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            assertThat(doc.getNumberOfPages()).isEqualTo(1);

            String extractedText = new PDFTextStripper().getText(doc);
            assertThat(extractedText).contains("Title text");
            assertThat(extractedText).contains("Body text");
        }

    }
}