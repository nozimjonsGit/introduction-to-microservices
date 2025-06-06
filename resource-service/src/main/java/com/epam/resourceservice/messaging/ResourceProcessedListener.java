package com.epam.resourceservice.messaging;

import com.epam.resourceservice.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceProcessedListener {

    private final ResourceService resourceService;

    @KafkaListener(
            topics = "${kafka.topic.resource-processed}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleProcessed(
            @Payload Long resourceId,
            @Header("X-B3-TraceId") String traceId,
            @Header("X-B3-SpanId")  String spanId
    ) {
        MDC.put("X-B3-TraceId", traceId);
        MDC.put("X-B3-SpanId",  spanId);

        log.info("Received resource-processed for id={}", resourceId);
        try {
            resourceService.markProcessed(resourceId);
            log.info("Resource {} moved to PERMANENT storage", resourceId);
        } catch (Exception ex) {
            log.error("Error in processed listener for id={}", resourceId, ex);
            throw ex;
        } finally {
            MDC.clear();
        }
    }
}
