package com.epam.resourceservice.service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.InputStream;

public interface S3Service {
    String uploadFile(String key, InputStream inputStream, long contentLength, String contentType);
    void deleteFile(String key);
    ResponseInputStream<GetObjectResponse> getFile(String key);
    byte[] downloadFile(String key);
}
