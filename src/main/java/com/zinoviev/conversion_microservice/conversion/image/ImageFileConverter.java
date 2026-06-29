package com.zinoviev.conversion_microservice.conversion.image;

import com.zinoviev.conversion_microservice.common.exception.conversion.TxtConversionException;
import com.zinoviev.conversion_microservice.conversion.api.FileConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;

@Component
@Slf4j
public class ImageFileConverter implements FileConverter {

    private static final Set<String> SUPPORTED_EXTENSIONS =
            Set.of("png", "jpg", "jpeg");

    @Override
    public boolean supports(String fileExtension) {
        return SUPPORTED_EXTENSIONS.contains(fileExtension.toLowerCase());
    }

    @Override
    public byte[] convert(String fileExtension, byte[] fileContent) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileContent));
            try (PDDocument document = new PDDocument()) {
                PDImageXObject pdImage = JPEGFactory.createFromImage(document, image);
                PDPage page = new PDPage(
                        new PDRectangle(pdImage.getWidth(), pdImage.getHeight()));
                document.addPage(page);

                try (PDPageContentStream contentStream =
                             new PDPageContentStream(document, page)) {
                    contentStream.drawImage(pdImage, 0, 0,
                            pdImage.getWidth(), pdImage.getHeight());
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
