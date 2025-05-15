package com.epam.resourceservice.service.impl;

import com.epam.resourceservice.exception.custom.FileStorageException;
import com.epam.resourceservice.service.S3Service;

import java.io.IOException;
import java.io.InputStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    /**
     * Uploads the file from the provided InputStream to S3 and returns the generated file URL.
     */
    @Override
    public String uploadFile(String bucket, String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

            String fileUrl = s3Client.utilities()
                    .getUrl(builder -> builder.bucket(bucket).key(key))
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
    @Override
    public void deleteFile(String bucket, String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
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
    @Override
    public ResponseInputStream<GetObjectResponse> getFile(String bucket, String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
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
    @Override
    public byte[] downloadFile(String bucket, String key) {
        try (ResponseInputStream<GetObjectResponse> s3Stream = getFile(bucket, key)) {
            return s3Stream.readAllBytes();
        } catch (IOException e) {
            log.error("Error reading file from S3 with key {}", key, e);
            throw new FileStorageException("Error reading file from S3 for key " + key, e);
        }
    }

    /**
     * Moves a file within S3 from one bucket/key to another.
     */
    @Override
    public void moveFile(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
        try {
            // Copy the object to the new location
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(destinationBucket)
                    .destinationKey(destinationKey)
                    .build();
            s3Client.copyObject(copyRequest);

            // Delete the original object
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(sourceBucket)
                    .key(sourceKey)
                    .build();
            s3Client.deleteObject(deleteRequest);

            log.info("Moved file in S3 from {}/{} to {}/{}", sourceBucket, sourceKey, destinationBucket, destinationKey);
        } catch (SdkException e) {
            log.error("Failed to move file in S3 from {}/{} to {}/{}", sourceBucket, sourceKey, destinationBucket, destinationKey, e);
            throw new FileStorageException("Failed to move file in S3", e);
        }
    }
}
