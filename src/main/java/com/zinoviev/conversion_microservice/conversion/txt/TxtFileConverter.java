package com.zinoviev.conversion_microservice.conversion.txt;

import com.zinoviev.conversion_microservice.common.exception.conversion.TxtConversionException;
import com.zinoviev.conversion_microservice.conversion.api.FileConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
@Slf4j
public class TxtFileConverter implements FileConverter {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("txt");

    @Override
    public boolean supports(String fileExtension) {
        return SUPPORTED_EXTENSIONS.contains(fileExtension);
    }

    @Override
    public byte[] convert(byte[] fileBytes) {
        try {
            String text = new String(fileBytes, StandardCharsets.UTF_8);
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream =
                             new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    contentStream.newLineAtOffset(50, 750);

                    String[] lines = text.split("\n");
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
            throw new TxtConversionException("Failed to convert TXT file to PDF");
        }
    }
}
