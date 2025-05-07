package com.epam.songservice;

import com.epam.songservice.controller.SongController;
import com.epam.songservice.dto.SongDTO;
import com.epam.songservice.entity.Song;
import com.epam.songservice.service.SongService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = SongController.class)
public class SongContractBaseTest {

    private static final long SONG_ID = 1L;

    @MockBean
    private SongService songService;

    @Autowired
    private SongController songController;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.standaloneSetup(songController);

        var song = SongDTO.builder()
                .id(String.valueOf(SONG_ID))
                .name("Believer")
                .artist("Imagine Dragons")
                .album("Evolve")
                .duration("3:24")
                .year("2017")
                .build();

        when(songService.createSong(song))
                .thenReturn(Song.builder().id(SONG_ID).build());

        when(songService.getSongById(SONG_ID)).thenReturn(song);

        when(songService.deleteSongs(anyList()))
                .thenReturn(List.of(SONG_ID));
    }
}
