package com.epam.resourceprocessor.client;

import com.epam.resourceprocessor.dto.SongMetadataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class SongServiceClient {

    private final RestTemplate restTemplate;

    @Value("${song.service.url}")
    private String songServiceUrl;

    public void saveSongMetadata(SongMetadataDTO songMetadata) {
        restTemplate.postForEntity(songServiceUrl, songMetadata, Void.class);
    }
}
