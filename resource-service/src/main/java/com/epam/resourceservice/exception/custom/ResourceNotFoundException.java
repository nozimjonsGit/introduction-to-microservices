package com.epam.resourceservice.exception.custom;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message, Long id) {
        super(String.format(message, id));
    }
}
