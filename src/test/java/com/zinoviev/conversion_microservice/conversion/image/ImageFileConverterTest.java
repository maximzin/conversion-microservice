package com.zinoviev.conversion_microservice.conversion.image;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ImageFileConverterTest {

    private final ImageFileConverter converter = new ImageFileConverter();

    // Создаем картинку необходимого формата
    private byte[] createTestImage(String format) throws IOException {
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 100; y++) {
                image.setRGB(x, y, 0xFF0000);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    @Test
    @DisplayName("Конвертирует JPG файл в PDF")
    void convert_shouldConvertJpgToPdf() throws Exception {
        // given
        byte[] jpegBytes = createTestImage("jpeg");

        // when
        byte[] pdfBytes = converter.convert("jpeg", jpegBytes);

        // then
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Конвертирует PNG файл в PDF")
    void convert_shouldConvertPngToPdf() throws Exception {
        // given
        byte[] pngBytes = createTestImage("png");

        // when
        byte[] pdfBytes = converter.convert("png", pngBytes);

        // then
        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 5)).isEqualTo("%PDF-");

        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
        }
    }


}