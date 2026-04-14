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

    public String uploadFromBase64(String base64Data, String objectName) {
        try {
            // Extract content type and base64 payload
            String contentType = "image/png";
            String base64Payload = base64Data;

            if (base64Data.contains(",")) {
                String[] parts = base64Data.split(",");
                String header = parts[0];
                base64Payload = parts[1];

                // Extract content type from header (e.g., "data:image/png;base64")
                if (header.contains(":")) {
                    String typePart = header.split(":")[1];
                    if (typePart.contains(";")) {
                        contentType = typePart.split(";")[0];
                    } else {
                        contentType = typePart;
                    }
                }
            }

            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Payload);
            log.info("Decoded base64 image, size: {} bytes, contentType: {}", imageBytes.length, contentType);

            String ext = contentType.split("/")[1];
            String finalObjectName = "images/" + objectName + "." + ext;

            try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
                upload(bais, finalObjectName, contentType, imageBytes.length);
            }

            String presignedUrl = getPresignedUrl(finalObjectName);
            log.info("Successfully uploaded base64 image to MinIO: {}", finalObjectName);
            return presignedUrl;
        } catch (IllegalArgumentException e) {
            log.error("Invalid base64 data provided", e);
            throw new RuntimeException("Invalid base64 data: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to upload base64 image to MinIO", e);
            throw new RuntimeException("Failed to upload base64 image: " + e.getMessage(), e);
        }
    }

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

    public String uploadAudioFromUrl(String audioUrl) {
        try {
            log.info("Downloading audio from: {}", audioUrl);
            URL url = new URL(audioUrl);
            byte[] audioBytes;

            try (InputStream in = url.openStream()) {
                audioBytes = in.readAllBytes();
            }

            log.info("Downloaded {} bytes, uploading audio to MinIO", audioBytes.length);

            String objectName = "audio/" + UUID.randomUUID() + ".mp3";
            try (ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes)) {
                upload(bais, objectName, "audio/mpeg", audioBytes.length);
            }

            String presignedUrl = getPresignedUrl(objectName);
            log.info("Successfully uploaded audio to MinIO: {}", objectName);
            return presignedUrl;
        } catch (IOException e) {
            log.error("Failed to upload audio from URL: {}", audioUrl, e);
            throw new RuntimeException("Failed to upload audio: " + e.getMessage(), e);
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
