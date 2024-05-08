package karm.van.habr.service;

import karm.van.habr.dto.ImageData;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ImageCompressionService {
    private final RestTemplate restTemplate;

    public byte[] compressImage(byte[] image,String type) {
        ImageData imageData = new ImageData(image,type);
        ResponseEntity<byte[]> response = restTemplate.postForEntity("http://compress-image-microservice/compress", imageData, byte[].class);
        return response.getBody();
    }
}
