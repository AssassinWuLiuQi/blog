package com.scholarsmanuscript.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public String uploadFromUrl(String imageUrl) {
        try {
            log.info("Downloading image from: {}", imageUrl);
            URL url = new URL(imageUrl);
            byte[] imageBytes;

            try (InputStream in = url.openStream()) {
                imageBytes = in.readAllBytes();
            }

            log.info("Downloaded {} bytes, uploading to MinIO", imageBytes.length);

            String objectName = "images/" + UUID.randomUUID() + ".png";
            try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
                upload(bais, objectName, "image/png", imageBytes.length);
            }

            String presignedUrl = getPresignedUrl(objectName);
            log.info("Successfully uploaded to MinIO: {}", objectName);
            return presignedUrl;
        } catch (IOException e) {
            log.error("Failed to upload image from URL: {}", imageUrl, e);
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    public void upload(InputStream inputStream, String objectName, String contentType) {
        upload(inputStream, objectName, contentType, -1);
    }

    public void upload(InputStream inputStream, String objectName, String contentType, long size) {
        try {
            // Ensure bucket exists
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created bucket: {}", bucket);
            }

            if (size > 0) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .stream(inputStream, size, -1)
                                .contentType(contentType)
                                .build()
                );
            } else {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .stream(inputStream, -1, 10485760)
                                .contentType(contentType)
                                .build()
                );
            }
            log.info("Successfully uploaded object: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to upload to MinIO: {}", objectName, e);
            throw new RuntimeException("Failed to upload to MinIO: " + e.getMessage(), e);
        }
    }

    public String getPresignedUrl(String objectName) {
        return getPresignedUrl(objectName, 7, TimeUnit.DAYS);
    }

    public String getPresignedUrl(String objectName, int expiry, TimeUnit timeUnit) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(expiry, timeUnit)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get presigned URL: {}", objectName, e);
            throw new RuntimeException("Failed to get presigned URL: " + e.getMessage(), e);
        }
    }
}
