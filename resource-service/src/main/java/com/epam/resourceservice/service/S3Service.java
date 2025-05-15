package com.epam.resourceservice.service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.InputStream;

public interface S3Service {
    String uploadFile(String bucket, String key, InputStream inputStream, long contentLength, String contentType);
    void deleteFile(String bucket, String key);
    ResponseInputStream<GetObjectResponse> getFile(String bucket, String key);
    byte[] downloadFile(String bucket, String key);
    void moveFile(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey);
}
