package com.epam.resourceservice.service.impl;

import com.epam.resourceservice.entity.Resource;
import com.epam.resourceservice.exception.custom.ResourceNotFoundException;
import com.epam.resourceservice.exception.custom.ResourceProcessingException;
import com.epam.resourceservice.repository.ResourceRepository;
import com.epam.resourceservice.service.KafkaProducerService;
import com.epam.resourceservice.service.ResourceService;
import com.epam.resourceservice.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final S3Service s3Service;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public Resource createAndProcessResource(byte[] audio) {
        String key = UUID.randomUUID() + ".mp3";
        String fileUrl = null;
        try (InputStream audioStream = new ByteArrayInputStream(audio)) {
            fileUrl = s3Service.uploadFile(key, audioStream, audio.length, "audio/mpeg");

            Resource resource = new Resource();
            resource.setFileUrl(fileUrl);
            resource.setFileKey(key);
            resourceRepository.save(resource);

            kafkaProducerService.sendResourceUploadedMessage(resource.getId());
            return resource;
        } catch (Exception ex) {
            if (fileUrl != null) {
                try {
                    s3Service.deleteFile(key);
                    log.info("Compensation: Deleted S3 file with key {}", key);
                } catch (Exception deleteEx) {
                    log.error("Failed to delete S3 file with key {} during compensation.", key, deleteEx);
                }
            }
            throw new ResourceProcessingException("Failed to create and process resource", ex);
        }
    }

    @Override
    public byte[] getResourceFileById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID %d not found", id));
        try {
            return s3Service.downloadFile(resource.getFileKey());
        } catch (Exception ex) {
            throw new ResourceProcessingException("Failed to download file for resource ID " + id, ex);
        }
    }

    @Override
    public List<Long> deleteResources(List<Long> ids) {
        List<Resource> resources = resourceRepository.findAllById(ids);
        List<Long> deletedIds = new ArrayList<>();
        for (Resource resource : resources) {
            try {
                s3Service.deleteFile(resource.getFileKey());
                deletedIds.add(resource.getId());
            } catch (Exception ex) {
                log.error("Failed to delete file for resource with ID {}", resource.getId(), ex);
            }
        }
        resourceRepository.deleteAllById(deletedIds);
        return deletedIds;
    }
}
