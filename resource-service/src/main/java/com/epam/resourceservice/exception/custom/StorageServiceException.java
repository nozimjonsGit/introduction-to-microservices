package com.epam.resourceservice.exception.custom;

public class StorageServiceException extends RuntimeException {
    public StorageServiceException(String message) {
        super(message);
    }
}
