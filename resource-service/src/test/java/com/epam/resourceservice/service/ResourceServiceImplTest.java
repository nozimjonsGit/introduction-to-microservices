package com.epam.resourceservice.service;

import com.epam.resourceservice.entity.Resource;
import com.epam.resourceservice.exception.custom.ResourceProcessingException;
import com.epam.resourceservice.repository.ResourceRepository;
import com.epam.resourceservice.service.impl.ResourceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ResourceServiceImplTest {
    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private ResourceServiceImpl resourceService;

    private static byte [] AUDIO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AUDIO = "test audio data".getBytes();
    }

    @Test
    void createAndProcessResource_Success() {
        String expectedUrl = "http://resource-bucket/file.mp3";

        when(s3Service.uploadFile(anyString(), any(InputStream.class), eq(AUDIO.length), eq("audio/mpeg")))
                .thenReturn(expectedUrl);
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocationOnMock -> {
            Resource resource = invocationOnMock.getArgument(0);
            resource.setId(42L);
            return resource;
        });

        Resource resource = resourceService.createAndProcessResource(AUDIO);

        assertNotNull(resource);
        assertEquals(42L, resource.getId());
        verify(kafkaProducerService).sendResourceUploadedMessage(42L);
    }


    @Test
    void createAndProcessResource_SavingResourceFails_Compensation() {
        String expectedUrl = "http://resource-bucket/file.mp3";

        when(s3Service.uploadFile(anyString(), any(), anyLong(), anyString()))
                .thenReturn(expectedUrl);

        when(resourceRepository.save(any(Resource.class))).thenThrow(new RuntimeException("DB error"));

        ResourceProcessingException ex = assertThrows(ResourceProcessingException.class,
                () -> resourceService.createAndProcessResource(AUDIO));

        assertEquals("Failed to create and process resource", ex.getMessage());
        verify(kafkaProducerService, never()).sendResourceUploadedMessage(anyLong());
        verify(s3Service).deleteFile(anyString());
    }


    @Test
    void createAndProcessResource_UploadFails_NoCompensation() {
        doThrow(new RuntimeException("Upload failed"))
                .when(s3Service).uploadFile(anyString(), any(InputStream.class), anyLong(), anyString());

        ResourceProcessingException ex = assertThrows(ResourceProcessingException.class,
                () -> resourceService.createAndProcessResource(AUDIO));

        assertEquals("Failed to create and process resource", ex.getMessage());
        verify(resourceRepository, never()).save(any(Resource.class));
        verify(kafkaProducerService, never()).sendResourceUploadedMessage(anyLong());
        verify(s3Service, never()).deleteFile(anyString());
    }

    @Test
    void getResourceFileById_Success() {
        Long resourceId = 1L;
        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setFileKey("test-key");
        resource.setFileUrl("http://resource-bucket/test-key");

        when(resourceRepository.findById(resourceId)).thenReturn(java.util.Optional.of(resource));
        when(s3Service.downloadFile("test-key")).thenReturn("test audio data".getBytes());

        byte[] result = resourceService.getResourceFileById(resourceId);

        assertNotNull(result);
        assertEquals("test audio data", new String(result));
    }

    @Test
    void getResourceFileById_ResourceNotFound() {
        Long resourceId = 1L;

        when(resourceRepository.findById(resourceId)).thenReturn(java.util.Optional.empty());

        try {
            resourceService.getResourceFileById(resourceId);
        } catch (Exception e) {
            assertEquals(String.format("Resource with ID %d not found", resourceId), e.getMessage());
        }
    }

    @Test
    void deleteResources_Success() {
        Resource resource1 = new Resource();
        resource1.setId(1L);
        resource1.setFileKey("key1");

        Resource resource2 = new Resource();
        resource2.setId(2L);
        resource2.setFileKey("key2");

        when(resourceRepository.findAllById(anyList())).thenReturn(List.of(resource1, resource2));

        List<Long> deletedIds = resourceService.deleteResources(List.of(1L, 2L));

        assertEquals(List.of(1L, 2L), deletedIds);
        verify(s3Service).deleteFile("key1");
        verify(s3Service).deleteFile("key2");
    }

    @Test
    void deleteResources_ResourceNotFound() {
        when(resourceRepository.findAllById(anyList())).thenReturn(new ArrayList<>());

        List<Long> deletedIds = resourceService.deleteResources(List.of(1L, 2L));

        assertTrue(deletedIds.isEmpty());
    }
}
