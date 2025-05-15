package com.epam.resourceservice.service.impl;

import com.epam.resourceservice.client.StorageServiceClient;
import com.epam.resourceservice.dto.ResourceStorageDTO;
import com.epam.resourceservice.entity.OutboxEvent;
import com.epam.resourceservice.entity.Resource;
import com.epam.resourceservice.entity.enums.EventType;
import com.epam.resourceservice.entity.enums.StorageType;
import com.epam.resourceservice.exception.custom.ResourceNotFoundException;
import com.epam.resourceservice.exception.custom.ResourceProcessingException;
import com.epam.resourceservice.repository.OutboxEventRepository;
import com.epam.resourceservice.repository.ResourceRepository;
import com.epam.resourceservice.service.ResourceService;
import com.epam.resourceservice.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final StorageServiceClient storageServiceClient;
    private final S3Service s3Service;
    private final OutboxEventRepository outboxEventRepository;

    @Override
    public Resource createAndProcessResource(byte[] audio) {
        ResourceStorageDTO staging = storageServiceClient.getStorage(StorageType.STAGING);
        String key = UUID.randomUUID() + ".mp3";
        String fullPath = staging.getPath() + "/" + key;

        try {
            s3Service.uploadFile(
                    staging.getBucket(), fullPath,
                    new ByteArrayInputStream(audio),
                    audio.length, "audio/mpeg"
            );

            Resource resource = new Resource();
            resource.setFileKey(key);
            resource.setBucket(staging.getBucket());
            resource.setPath(staging.getPath());
            resource.setState(StorageType.STAGING.name());
            resourceRepository.save(resource);

            recordOutboxEvent(EventType.CREATE, resource.getId(), staging.getBucket(), staging.getPath(), key, audio);

            return resource;
        } catch (Exception ex) {
            try {
                s3Service.deleteFile(staging.getBucket(), fullPath);
                log.info("Compensation delete of {}/{} succeeded", staging.getBucket(), fullPath);
            } catch (Exception delEx) {
                log.error("Compensation delete of {}/{} failed", staging.getBucket(), fullPath, delEx);
            }
            throw new ResourceProcessingException("Failed to create and process resource", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getResourceFileById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID %d not found", id));
        try {
            String fullPath = resource.getPath() + "/" + resource.getFileKey();
            return s3Service.downloadFile(resource.getBucket(), fullPath);
        } catch (Exception ex) {
            throw new ResourceProcessingException("Failed to download file for resource ID " + id, ex);
        }
    }

    @Override
    public List<Long> deleteResources(List<Long> ids) {
        List<Resource> resources = resourceRepository.findAllById(ids);
        List<Long> deletedIds = new ArrayList<>();

        for (Resource resource : resources) {
            String fullPath = resource.getPath() + "/" + resource.getFileKey();
            try {
                s3Service.deleteFile(resource.getBucket(), fullPath);

                deletedIds.add(resource.getId());

                recordOutboxEvent(EventType.DELETE, resource.getId(), resource.getBucket(),
                        resource.getPath(), resource.getFileKey(), null);

            } catch (Exception ex) {
                log.error("Failed to delete resource {}/{}", resource.getBucket(), fullPath, ex);
            }
        }

        resourceRepository.deleteAllById(deletedIds);
        return deletedIds;
    }

    @Override
    public void markProcessed(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID %d not found", resourceId));

        ResourceStorageDTO permanentStorage = storageServiceClient.getStorage(StorageType.PERMANENT);
        String source = resource.getPath() + "/" + resource.getFileKey();
        String destination = permanentStorage.getPath() + "/" + resource.getFileKey();

        s3Service.moveFile(resource.getBucket(), source, permanentStorage.getBucket(), destination);

        resource.setBucket(permanentStorage.getBucket());
        resource.setPath(permanentStorage.getPath());
        resource.setState(StorageType.PERMANENT.name());
        resourceRepository.save(resource);
    }

    private void recordOutboxEvent(EventType type, Long resourceId, String bucket,
                              String path, String key, byte[] data) {
        var outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(type);
        outboxEvent.setResourceId(resourceId);
        outboxEvent.setBucket(bucket);
        outboxEvent.setPath(path);
        outboxEvent.setFileKey(key);
        outboxEvent.setResourceData(data);
        outboxEventRepository.save(outboxEvent);
    }
}
