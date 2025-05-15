package com.epam.songservice.messaging;

import com.epam.songservice.service.SongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceDeletedListener {

    private final SongService songService;

    @KafkaListener(
            topics = "${kafka.topic.resource-deleted}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onResourceDeleted(Long resourceId) {
        log.info("Received resource-deleted event for resourceId={}", resourceId);
        try {
            songService.deleteSongById(resourceId);
            log.info("Deleted song metadata for resourceId={}", resourceId);
        } catch (Exception ex) {
            log.error("Failed to delete song metadata for resourceId={}: {}", resourceId, ex.getMessage(), ex);
            throw ex;
        }
    }
}
