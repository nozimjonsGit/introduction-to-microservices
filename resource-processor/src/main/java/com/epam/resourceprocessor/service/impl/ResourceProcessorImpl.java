package com.epam.resourceprocessor.service.impl;

import com.epam.resourceprocessor.client.ResourceServiceClient;
import com.epam.resourceprocessor.client.SongServiceClient;
import com.epam.resourceprocessor.dto.SongMetadataDTO;
import com.epam.resourceprocessor.messaging.ResourceProcessedProducer;
import com.epam.resourceprocessor.service.ResourceProcessor;
import com.epam.resourceprocessor.util.SongMetadataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceProcessorImpl implements ResourceProcessor {

    private final ResourceServiceClient resourceServiceClient;
    private final SongServiceClient songServiceClient;
    private final ResourceProcessedProducer resourceProcessedProducer;

    @Override
    @KafkaListener(topics = "${kafka.topic.resource-uploaded}", groupId = "${kafka.consumer.group-id}")
    @Retryable(
            noRetryFor = { IllegalArgumentException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000)
    )
    public void process(
            @Payload Long resourceId,
            @Header("X-B3-TraceId") String traceId,
            @Header("X-B3-SpanId")  String spanId
    ) {
        MDC.put("X-B3-TraceId", traceId);
        MDC.put("X-B3-SpanId",  spanId);

        log.info("Starting processing received message [{}].", resourceId);
        try {
            byte[] data = resourceServiceClient.getResourceData(resourceId);

            SongMetadataDTO meta = SongMetadataUtil.extractMetadata(
                    new ByteArrayInputStream(data)
            );
            meta.setId(String.valueOf(resourceId));
            log.info("Successfully extracted song metadata [{}].", meta);

            songServiceClient.saveSongMetadata(meta);

            resourceProcessedProducer.publishProcessed(
                    resourceId, traceId, spanId);

            log.info("Processed resource {} successfully.", resourceId);
        } catch (Exception e) {
            log.error("Error processing resource {}: {}", resourceId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
