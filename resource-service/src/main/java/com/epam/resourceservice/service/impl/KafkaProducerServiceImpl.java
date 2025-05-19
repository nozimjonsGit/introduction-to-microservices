package com.epam.resourceservice.service.impl;

import com.epam.resourceservice.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, Long> kafkaTemplate;

    @Value("${kafka.topic.resource-uploaded}")
    private String uploadedTopic;

    @Value("${kafka.topic.resource-deleted}")
    private String deletedTopic;

    @Override
    public void sendResourceUploadedMessage(Long resourceId, String traceId, String spanId) {
        Message<Long> message = MessageBuilder.withPayload(resourceId)
                .setHeader(KafkaHeaders.TOPIC, uploadedTopic)
                .setHeader("X-B3-TraceId", traceId)
                .setHeader("X-B3-SpanId", spanId)
                .build();
        kafkaTemplate.send(message);
    }

    @Override
    public void sendResourceDeletedMessage(Long resourceId, String traceId, String spanId) {
        Message<Long> message = MessageBuilder.withPayload(resourceId)
                .setHeader(KafkaHeaders.TOPIC, deletedTopic)
                .setHeader("X-B3-TraceId", traceId)
                .setHeader("X-B3-SpanId", spanId)
                .build();
        kafkaTemplate.send(message);
    }
}
