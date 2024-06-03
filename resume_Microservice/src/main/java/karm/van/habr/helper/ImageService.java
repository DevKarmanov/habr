package karm.van.habr.helper;

import io.minio.errors.*;
import karm.van.habr.entity.Complaint;
import karm.van.habr.entity.ImageResume;
import karm.van.habr.entity.Resume;
import karm.van.habr.exceptions.ImageTroubleException;
import karm.van.habr.repo.ImageResumeRepo;
import karm.van.habr.service.ImageCompressionService;
import karm.van.habr.service.MinioServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
public class ImageService {

    public static void imageSave(MultipartFile[] files, Resume resume, String bucketName, MinioServer minioServer, ImageResumeRepo imageResumeRepo, ImageCompressionService imageCompressionService) throws ImageTroubleException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        saveImages(files, resume, null, bucketName, minioServer, imageResumeRepo, imageCompressionService);
    }

    public static void complaintImageSave(MultipartFile[] files, Complaint complaint, String bucketName, MinioServer minioServer, ImageResumeRepo imageResumeRepo, ImageCompressionService imageCompressionService) throws ImageTroubleException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        saveImages(files, null, complaint, bucketName, minioServer, imageResumeRepo, imageCompressionService);
    }

    private static void saveImages(MultipartFile[] files, Resume resume, Complaint complaint, String bucketName, MinioServer minioServer, ImageResumeRepo imageResumeRepo, ImageCompressionService imageCompressionService) throws ImageTroubleException, ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        ExecutorService executorService = Executors.newFixedThreadPool(files.length);
        List<Callable<String>> tasks = getCallableList(files, bucketName, resume != null ? resume.getId() : complaint.getId(), imageCompressionService, minioServer);

        minioServer.createBucketIfNotExist(bucketName);

        try {
            List<Future<String>> results = executorService.invokeAll(tasks);
            for (Future<String> result : results) {
                ImageResume imageResume = ImageResume.builder()
                        .resume(resume)
                        .complaint(complaint)
                        .objectName(result.get())
                        .bucketName(bucketName)
                        .build();
                imageResumeRepo.save(imageResume);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error processing images: {}", e.getMessage());
            throw new ImageTroubleException("Какая-то проблема с обработкой изображений. Приносим свои извинения. Попробуйте перезагрузить страницу и предоставить новые");
        } finally {
            executorService.shutdown();
        }
    }

    private static List<Callable<String>> getCallableList(MultipartFile[] files, String bucketName, Long id, ImageCompressionService imageCompressionService, MinioServer minioServer) {
        List<Callable<String>> tasks = new ArrayList<>();
        for (MultipartFile file : files) {
            tasks.add(() -> {
                byte[] compressedImage = imageCompressionService.compressImage(file.getBytes(), file.getContentType());
                if (compressedImage == null) {
                    throw new RuntimeException("Сжатие не удалось для: " + file.getOriginalFilename());
                }
                InputStream inputStream = new ByteArrayInputStream(compressedImage);
                String fileName = generateNameForImage(file, id);
                minioServer.uploadFile(bucketName, fileName, inputStream, compressedImage.length, file.getContentType());
                return fileName;
            });
        }
        return tasks;
    }

    private static String generateNameForImage(MultipartFile file, Long id) {
        return UUID.randomUUID() + "_" + file.getOriginalFilename() + "_" + id;
    }
}
