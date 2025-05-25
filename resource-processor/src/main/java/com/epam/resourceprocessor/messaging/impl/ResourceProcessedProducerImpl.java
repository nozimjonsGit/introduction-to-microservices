package com.epam.resourceprocessor.messaging.impl;

import com.epam.resourceprocessor.messaging.ResourceProcessedProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceProcessedProducerImpl implements ResourceProcessedProducer {

    private final KafkaTemplate<String, Long> kafkaTemplate;

    @Value("${kafka.topic.resource-processed}")
    private String topic;

    @Override
    public void publishProcessed(Long resourceId, String traceId, String spanId) {
        Message<Long> message = MessageBuilder.withPayload(resourceId)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("X-B3-TraceId", traceId)
                .setHeader("X-B3-SpanId", spanId)
                .build();
        kafkaTemplate.send(message);
    }
}
