package com.epam.songservice.service;

import com.epam.songservice.dto.SongDTO;
import com.epam.songservice.entity.Song;

import java.util.List;

public interface SongService {
    Song createSong(SongDTO song);
    SongDTO getSongById(Long id);
    List<Long> deleteSongs(List<Long> ids);
    void deleteSongById(Long id);
}
