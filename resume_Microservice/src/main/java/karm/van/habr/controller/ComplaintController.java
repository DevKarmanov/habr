package karm.van.habr.controller;

import karm.van.habr.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/resume_v1")
public class ComplaintController {
    private final ComplaintService complaintService;

    @Async
    @PostMapping("/create-profile-complaint")
    public CompletableFuture<ResponseEntity<String>> createProfileComplaint(@RequestParam(name = "problemDescription") String problemDescription,
                                                                            @RequestParam(name = "images",required = false) Optional<MultipartFile[]> images,
                                                                            @RequestParam(name = "author_name") String author_name,
                                                                            @RequestParam(name = "inspect_name") String inspect_name){
        return CompletableFuture.supplyAsync(()->{
           try {
               complaintService.saveComplaint(problemDescription,images,author_name,inspect_name);
               return ResponseEntity.ok("Отправили администраторам");
           }catch (Exception e){
               return ResponseEntity.badRequest().body(e.getMessage());
           }
        });
    }

    @GetMapping("/complain-images/{complaintId}/{imageId}")
    public ResponseEntity<byte[]> getResumeImages(@PathVariable Long complaintId, @PathVariable int imageId) {
        try {
            InputStream imageStream = complaintService.getMinioImageComplaint(complaintId, imageId);
            if (imageStream == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = imageStream.readAllBytes();
            imageStream.close();

            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
