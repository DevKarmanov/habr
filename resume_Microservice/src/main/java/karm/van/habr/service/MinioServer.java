package karm.van.habr.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MinioServer {
    private final MinioClient minioClient;

    public void createBucketIfNotExist(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())){
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public void uploadFile(String bucketName, String objectName, InputStream inputStream, long size, String contentType) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, size, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    public InputStream downloadFile(String bucketName, String objectName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    public void deleteFile(String bucketName, String objectName) throws Exception {
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(List.of(new DeleteObject(objectName)))
                        .build()
        );

        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            if (error != null) {
                throw new RuntimeException("Ошибка при удалении объекта: " + error.message());
            }
        }
    }

    public void deleteFiles(String bucketName, List<String> objectNames) throws Exception {
        List<DeleteObject> objects = objectNames.stream()
                .map(DeleteObject::new)
                .toList();

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(objects)
                        .build()
        );

        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            if (error != null) {
                throw new RuntimeException("Ошибка при удалении объекта: " + error.message());
            }
        }
    }
}
