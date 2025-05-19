package com.epam.resourceservice.service;

public interface KafkaProducerService {
    void sendResourceUploadedMessage(Long resourceId, String traceId, String spanId);
    void sendResourceDeletedMessage(Long resourceId, String traceId, String spanId);
}
