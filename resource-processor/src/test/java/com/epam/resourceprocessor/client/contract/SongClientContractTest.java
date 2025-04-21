package com.epam.resourceprocessor.client.contract;

import com.epam.resourceprocessor.dto.SongMetadataDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.LOCAL,
        ids = "com.epam:song-service:+:stubs:8081")
@EmbeddedKafka(
        partitions = 1,
        topics = { "resource-uploaded" })
@SpringBootTest
public class SongClientContractTest {

    private static final String SONG_BASE_URI = "http://localhost:8081/songs";
    private static final int SONG_ID = 1;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void pingStub() {
        ResponseEntity<Void> response =
                restTemplate.getForEntity("http://localhost:8081/ping", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createSongMetadata() {
        // given
        var songMetadata = SongMetadataDTO.builder()
                .id(String.valueOf(SONG_ID))
                .name("Believer")
                .artist("Imagine Dragons")
                .album("Evolve")
                .duration("3:24")
                .year("2017")
                .build();

        // when
        var response =
                restTemplate.postForEntity(SONG_BASE_URI, songMetadata, Map.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertNotNull(response.getBody());
        assertThat(response.getBody().get("id")).isEqualTo(SONG_ID);
    }

    @Test
    void getSongMetadata() {
        // given
        var songMetadata = SongMetadataDTO.builder()
                .id(String.valueOf(SONG_ID))
                .name("Believer")
                .artist("Imagine Dragons")
                .album("Evolve")
                .duration("3:24")
                .year("2017")
                .build();

        // when
        var response =
                restTemplate.getForEntity(SONG_BASE_URI + "/" + SONG_ID, SongMetadataDTO.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(songMetadata);
    }
}
