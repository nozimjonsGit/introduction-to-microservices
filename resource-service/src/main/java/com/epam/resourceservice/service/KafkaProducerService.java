package com.epam.resourceservice.service;

public interface KafkaProducerService {
    void sendResourceUploadedMessage(Long resourceId);
}
