package karm.van.controller;

import karm.van.dao.ImageData;
import karm.van.service.CompressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CompressController {
    private final CompressService compressService;

    @PostMapping("/compress")
    public byte[] compress(@RequestBody ImageData imageData) {
        return compressService.compress(imageData);
    }
}
