package com.healthcare.service;

import com.healthcare.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket.name:healthcare-records}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String folder, Map<String, String> metadata) {
        String fileKey = generateSecureFileKey(folder, file.getOriginalFilename());

        try {
            Map<String, String> fileMetadata = new HashMap<>(metadata);
            fileMetadata.put("original-filename", file.getOriginalFilename());
            fileMetadata.put("content-type", file.getContentType());
            fileMetadata.put("upload-timestamp", LocalDateTime.now().toString());

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .metadata(fileMetadata)
                    .build();

            s3Client.putObject(putRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("File uploaded successfully to S3: {}", fileKey);
            return fileKey;

        } catch (IOException e) {
            log.error("Failed to upload file to S3: {}", fileKey, e);
            throw new FileStorageException("Failed to store file: " + e.getMessage());
        } catch (S3Exception e) {
            log.error("S3 error while uploading file: {}", fileKey, e);
            throw new FileStorageException("S3 storage error: " + e.getMessage());
        }
    }

    public byte[] downloadFile(String fileKey) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getRequest);

            log.info("File downloaded successfully from S3: {}", fileKey);
            return objectBytes.asByteArray();

        } catch (S3Exception e) {
            log.error("Failed to download file from S3: {}", fileKey, e);
            throw new FileStorageException("Failed to retrieve file: " + e.getMessage());
        }
    }

    public void deleteFile(String fileKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteRequest);

            log.info("File deleted successfully from S3: {}", fileKey);

        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {}", fileKey, e);
            throw new FileStorageException("Failed to delete file: " + e.getMessage());
        }
    }

    public String generatePresignedUrl(String fileKey, int expirationMinutes) {
        // Note: For production, implement presigned URL generation
        // This is a simplified version
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileKey);
    }

    private String generateSecureFileKey(String folder, String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return String.format("%s/%s_%s%s", folder, timestamp, randomId, extension);
    }
}
