package com.epam.resourceservice.service.scheduler;

import com.epam.resourceservice.entity.OutboxEvent;
import com.epam.resourceservice.entity.enums.EventType;
import com.epam.resourceservice.repository.OutboxEventRepository;
import com.epam.resourceservice.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Runs every 5 seconds (after an initial delay of 10s),
     * publishes all pending events, and deletes them on success.
     */
    @Scheduled(initialDelayString = "${outbox.scheduler.initial-delay:10000}",
            fixedRateString    = "${outbox.scheduler.fixed-rate:5000}")
    @Transactional
    public void publishPendingOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findAll();
        for (OutboxEvent event : events) {
            String traceId = event.getTraceId();
            String spanId  = event.getSpanId();
            MDC.put("X-B3-TraceId", traceId);
            MDC.put("X-B3-SpanId",  spanId);

            try {
                if (event.getEventType() == EventType.CREATE) {
                    kafkaProducerService.sendResourceUploadedMessage(
                            event.getResourceId(), traceId, spanId);
                } else {
                    kafkaProducerService.sendResourceDeletedMessage(
                            event.getResourceId(), traceId, spanId);
                }

                outboxEventRepository.delete(event);
                log.debug("Published and removed outbox event id={} type={}", event.getId(), event.getEventType());
            } catch (Exception ex) {
                log.error("Failed to publish outbox event id={} type={}", event.getId(), event.getEventType(), ex);
            } finally {
                MDC.clear();
            }
        }
    }
}
