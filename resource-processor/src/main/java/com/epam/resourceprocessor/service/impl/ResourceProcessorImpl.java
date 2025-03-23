package com.epam.resourceprocessor.service.impl;

import com.epam.resourceprocessor.client.ResourceServiceClient;
import com.epam.resourceprocessor.client.SongServiceClient;
import com.epam.resourceprocessor.dto.SongMetadataDTO;
import com.epam.resourceprocessor.service.ResourceProcessor;
import com.epam.resourceprocessor.util.SongMetadataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
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

    @Override
    @KafkaListener(topics = "${kafka.topic.resource-uploaded}", groupId = "${kafka.consumer.group-id}")
    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000)
    )
    public void process(Long resourceId) {
        log.info("Starting processing received message [{}].", resourceId);

        byte[] resourceData = resourceServiceClient.getResourceData(resourceId);
        try {
            SongMetadataDTO songMetadataDTO = SongMetadataUtil.extractMetadata(new ByteArrayInputStream(resourceData));
            songMetadataDTO.setId(String.valueOf(resourceId));
            log.info("Successfully extracted song metadata [{}].", songMetadataDTO);

            songServiceClient.saveSongMetadata(songMetadataDTO);
            log.info("Processed resource {} successfully.", resourceId);
        } catch (Exception e) {
            log.error("Error processing resource {}: {}", resourceId, e.getMessage(), e);
            throw e;
        }
    }
}
