package karm.van.service;

import karm.van.dao.ImageData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

@Service
@Slf4j
public class CompressService {

    public byte[] compress(ImageData imageData) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData.imageByte());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            BufferedImage originalImage = ImageIO.read(inputStream);

            String fileType = imageData.type().toLowerCase().substring(6);
            log.info(fileType);
            log.info(imageData.type());

            ImageIO.write(originalImage, fileType, outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
