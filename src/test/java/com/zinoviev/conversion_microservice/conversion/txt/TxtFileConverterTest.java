package com.zinoviev.conversion_microservice.conversion.txt;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TxtFileConverterTest {

    private final TxtFileConverter converter = new TxtFileConverter();

    @Test
    @DisplayName("Конвертирует TXT файл в PDF")
    void convert_shouldConvertTxtToPdf() throws IOException {
        // given
        String text = "Title text\nBody text";
        byte[] input = text.getBytes(StandardCharsets.UTF_8);

        // when
        byte[] pdfBytes = converter.convert(input);

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