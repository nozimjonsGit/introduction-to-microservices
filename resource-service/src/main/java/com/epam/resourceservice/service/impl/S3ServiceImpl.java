package com.epam.resourceservice.service.impl;

import com.epam.resourceservice.exception.custom.FileStorageException;
import com.epam.resourceservice.service.S3Service;

import java.io.IOException;
import java.io.InputStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Uploads the file from the provided InputStream to S3 and returns the generated file URL.
     */
    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

            String fileUrl = s3Client.utilities()
                    .getUrl(builder -> builder.bucket(bucketName).key(key))
                    .toExternalForm();
            log.info("File uploaded successfully to S3: {}", fileUrl);
            return fileUrl;
        } catch (SdkException e) {
            log.error("Failed to upload file to S3", e);
            throw new FileStorageException("Failed to upload file to S3", e);
        }
    }

    /**
     * Deletes a file from S3 using the provided key.
     */
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("File with key {} deleted from S3", key);
        } catch (SdkException e) {
            log.error("Failed to delete file with key {} from S3", key, e);
            throw new FileStorageException("Failed to delete file from S3 for key " + key, e);
        }
    }

    /**
     * Retrieves the file from S3 as a stream.
     */
    public ResponseInputStream<GetObjectResponse> getFile(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            return s3Client.getObject(getObjectRequest);
        } catch (SdkException e) {
            log.error("Failed to retrieve file from S3 with key {}", key, e);
            throw new FileStorageException("Failed to retrieve file from S3 for key " + key, e);
        }
    }

    /**
     * Downloads the file from S3 and returns its contents as a byte array.
     */
    public byte[] downloadFile(String key) {
        try (ResponseInputStream<GetObjectResponse> s3Stream = getFile(key)) {
            return s3Stream.readAllBytes();
        } catch (IOException e) {
            log.error("Error reading file from S3 with key {}", key, e);
            throw new FileStorageException("Error reading file from S3 for key " + key, e);
        }
    }
}
