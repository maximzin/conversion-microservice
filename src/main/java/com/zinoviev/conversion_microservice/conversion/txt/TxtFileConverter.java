package com.zinoviev.conversion_microservice.conversion.txt;

import com.zinoviev.conversion_microservice.conversion.api.FileConverter;
import com.zinoviev.conversion_microservice.core.exception.conversion.TxtConversionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
@Slf4j
public class TxtFileConverter implements FileConverter {

    @Value("${pdfbox.custom-font}")
    String pdfBoxCustomFontPath;

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("txt");

    @Override
    public boolean supports(String fileExtension) {
        return SUPPORTED_EXTENSIONS.contains(fileExtension);
    }

    @Override
    public byte[] convert(String fileExtension, byte[] fileBytes) {
        try {
            String text = new String(fileBytes, StandardCharsets.UTF_8);
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                PDFont font;
                try (InputStream fontStream = getClass()
                        .getResourceAsStream(pdfBoxCustomFontPath)) {
                    if (fontStream == null) {
                        throw new IOException(String.format("Файл шрифта не найден: %s", pdfBoxCustomFontPath));
                    }
                    font = PDType0Font.load(document, fontStream, true);
                }

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.newLineAtOffset(50, 750);

                    String[] lines = text.replace("\r", "").split("\n");
                    for (String line : lines) {
                        contentStream.showText(line);
                        contentStream.newLineAtOffset(0, -14.5f);
                    }
                    contentStream.endText();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                document.save(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            log.error("Не удалось сконвертировать файл {} в PDF", fileExtension.toUpperCase(), e);
            throw new TxtConversionException(String.format("Не удалось сконвертировать файл %s в PDF", fileExtension.toUpperCase()), e);
        }
    }


}
