package com.epam.resourceservice.service;

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
import com.epam.resourceservice.service.impl.ResourceServiceImpl;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private StorageServiceClient storageServiceClient;

    @Mock
    private S3Service s3Service;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private ResourceServiceImpl underTest;

    private byte[] sampleAudio;

    private ResourceStorageDTO staging;

    @BeforeEach
    void setUp() {
        sampleAudio = "dummy-audio".getBytes();
        staging = new ResourceStorageDTO(1L, StorageType.STAGING,
                "staging-bucket", "staging-path");
    }

    @Test
    @DisplayName("createAndProcessResource: successful upload records CREATE event")
    void createAndProcessResource_successRecordsOutboxEvent() {
        // given
        given(storageServiceClient.getStorage(StorageType.STAGING)).willReturn(staging);

        given(s3Service.uploadFile(
                eq("staging-bucket"),
                startsWith("staging-path/"),
                any(ByteArrayInputStream.class),
                eq((long) sampleAudio.length),
                eq("audio/mpeg")
        )).willReturn("unused-url");

        ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
        willAnswer(inv -> {
            var r = inv.getArgument(0, Resource.class);
            r.setId(101L);
            return r;
        }).given(resourceRepository).save(resourceCaptor.capture());

        // when
        Resource result = underTest.createAndProcessResource(sampleAudio);

        // then
        BDDAssertions.then(result.getId()).isEqualTo(101L);

        Resource saved = resourceCaptor.getValue();
        BDDAssertions.then(saved.getBucket()).isEqualTo("staging-bucket");
        BDDAssertions.then(saved.getPath()).isEqualTo("staging-path");
        BDDAssertions.then(saved.getState()).isEqualTo(StorageType.STAGING.name());
        BDDAssertions.then(saved.getFileKey()).endsWith(".mp3");

        ArgumentCaptor<OutboxEvent> evtCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        then(outboxEventRepository).should().save(evtCaptor.capture());
        OutboxEvent evt = evtCaptor.getValue();
        BDDAssertions.then(evt.getEventType()).isEqualTo(EventType.CREATE);
        BDDAssertions.then(evt.getResourceId()).isEqualTo(101L);
        BDDAssertions.then(evt.getBucket()).isEqualTo("staging-bucket");
        BDDAssertions.then(evt.getPath()).isEqualTo("staging-path");
        BDDAssertions.then(evt.getFileKey()).isEqualTo(saved.getFileKey());
        BDDAssertions.then(evt.getResourceData()).containsExactly(sampleAudio);
    }

    @Test
    @DisplayName("createAndProcessResource: DB save fails compensates and skips outbox")
    void createAndProcessResource_saveFailsCompensatesAndSkipsOutbox() {
        // given
        given(storageServiceClient.getStorage(StorageType.STAGING)).willReturn(staging);

        given(s3Service.uploadFile(anyString(), anyString(), any(), anyLong(), anyString()))
                .willReturn("dummy-url");
        given(resourceRepository.save(any(Resource.class)))
                .willThrow(new RuntimeException("db-error"));

        // when / then
        ResourceProcessingException ex = assertThrows(
                ResourceProcessingException.class,
                () -> underTest.createAndProcessResource(sampleAudio)
        );
        BDDAssertions.then(ex).hasMessage("Failed to create and process resource");

        then(s3Service).should().deleteFile(
                eq("staging-bucket"),
                startsWith("staging-path/")
        );
        then(outboxEventRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("createAndProcessResource: upload fails skips persistence and outbox")
    void createAndProcessResource_uploadFailsSkipsPersistenceAndOutbox() {
        // given
        given(storageServiceClient.getStorage(StorageType.STAGING)).willReturn(staging);

        willThrow(new RuntimeException("upload-error"))
                .given(s3Service).uploadFile(anyString(), anyString(), any(), anyLong(), anyString());

        // when / then
        assertThrows(ResourceProcessingException.class,
                () -> underTest.createAndProcessResource(sampleAudio));

        then(resourceRepository).should(never()).save(any());
        then(outboxEventRepository).should(never()).save(any());
        then(s3Service).should().deleteFile(
                eq("staging-bucket"),
                startsWith("staging-path/")
        );
    }

    @Test
    @DisplayName("getResourceFileById: returns bytes when resource exists")
    void getResourceFileById_returnsBytesWhenExists() {
        // given
        Long id = 5L;
        var res = new Resource();
        res.setId(id);
        res.setBucket("b");
        res.setPath("p");
        res.setFileKey("k.mp3");
        given(resourceRepository.findById(id)).willReturn(Optional.of(res));
        given(s3Service.downloadFile("b", "p/k.mp3"))
                .willReturn("data".getBytes());

        // when
        byte[] data = underTest.getResourceFileById(id);

        // then
        BDDAssertions.then(data).containsExactly("data".getBytes());
    }

    @Test
    @DisplayName("getResourceFileById: throws when missing")
    void getResourceFileById_throwsWhenMissing() {
        // given
        given(resourceRepository.findById(99L)).willReturn(Optional.empty());

        // when / then
        assertThrows(ResourceNotFoundException.class,
                () -> underTest.getResourceFileById(99L));
    }

    @Test
    @DisplayName("deleteResources: deletes, records DELETE events, and returns IDs")
    void deleteResources_deletesAndRecordsEvents() {
        // given
        Resource r1 = new Resource(); r1.setId(1L); r1.setBucket("b1"); r1.setPath("p1"); r1.setFileKey("k1");
        Resource r2 = new Resource(); r2.setId(2L); r2.setBucket("b2"); r2.setPath("p2"); r2.setFileKey("k2");
        given(resourceRepository.findAllById(List.of(1L, 2L)))
                .willReturn(List.of(r1, r2));

        // when
        List<Long> deleted = underTest.deleteResources(List.of(1L, 2L));

        // then
        BDDAssertions.then(deleted).containsExactly(1L, 2L);
        then(s3Service).should().deleteFile("b1", "p1/k1");
        then(s3Service).should().deleteFile("b2", "p2/k2");

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        then(outboxEventRepository).should(times(2)).save(captor.capture());
        BDDAssertions.then(captor.getAllValues())
                .extracting(OutboxEvent::getEventType, OutboxEvent::getResourceId)
                .containsExactly(
                        tuple(EventType.DELETE, 1L),
                        tuple(EventType.DELETE, 2L)
                );

        then(resourceRepository).should().deleteAllById(List.of(1L, 2L));
    }

    @Test
    @DisplayName("deleteResources: returns empty when none found")
    void deleteResources_returnsEmptyWhenNoneFound() {
        // given
        given(resourceRepository.findAllById(anyList())).willReturn(List.of());

        // when
        List<Long> deleted = underTest.deleteResources(List.of(3L, 4L));

        // then
        BDDAssertions.then(deleted).isEmpty();
        then(s3Service).should(never()).deleteFile(anyString(), anyString());
        then(outboxEventRepository).should(never()).save(any());
        then(resourceRepository).should().deleteAllById(List.of());
    }
}
