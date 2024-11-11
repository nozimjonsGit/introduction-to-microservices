package com.epam.resourceservice.service.impl;

import com.epam.resourceservice.dto.SongMetadataDTO;
import com.epam.resourceservice.entity.Resource;
import com.epam.resourceservice.exception.custom.ResourceNotFoundException;
import com.epam.resourceservice.repository.ResourceRepository;
import com.epam.resourceservice.service.ResourceService;
import com.epam.resourceservice.util.SongMetadataUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final RestTemplate restTemplate;
    private static final String SONG_CLIENT_SERVICE = "http://localhost:8081/songs";

    @Override
    public Resource createAndProcessResource(byte[] audio) throws TikaException, SAXException, IOException {
        try (InputStream audioStream = new ByteArrayInputStream(audio)) {
            Resource resource = new Resource();
            resource.setAudioData(audio);
            resourceRepository.save(resource);

            SongMetadataDTO songMetadataDTO = SongMetadataUtil.extractMetadata(audioStream);
            songMetadataDTO.setResourceId(resource.getId());

            restTemplate.postForObject(SONG_CLIENT_SERVICE, songMetadataDTO, Void.class);

            return resource;
        }
    }

    public Resource getResourceById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID %d not found", id));
    }

    public List<Long> deleteResources(List<Long> ids) {
        List<Long> deletedIds = new ArrayList<>();
        for (Long id : ids) {
            if (resourceRepository.existsById(id)) {
                resourceRepository.deleteById(id);
                deletedIds.add(id);
            }
        }
        return deletedIds;
    }
}
