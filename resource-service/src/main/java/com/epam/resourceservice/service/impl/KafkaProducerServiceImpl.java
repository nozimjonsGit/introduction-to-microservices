package com.epam.resourceservice.service.impl;

import com.epam.resourceservice.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, Long> kafkaTemplate;

    @Value("${kafka.topic.resource-uploaded}")
    private String topic;

    @Override
    public void sendResourceUploadedMessage(Long resourceId) {
        kafkaTemplate.send(topic, resourceId);
    }
}
