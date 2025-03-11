package com.epam.resourceservice.exception.custom;

public class ResourceProcessingException extends RuntimeException {
    public ResourceProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
