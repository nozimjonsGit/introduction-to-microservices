package com.epam.songservice.service.impl;

import com.epam.songservice.dto.SongDTO;
import com.epam.songservice.entity.Song;
import com.epam.songservice.exception.custom.DuplicateRecordException;
import com.epam.songservice.exception.custom.SongNotFoundException;
import com.epam.songservice.repository.SongRepository;
import com.epam.songservice.service.SongService;
import com.epam.songservice.util.mapper.SongMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final SongMapper songMapper;

    @Override
    @Transactional
    public Song createSong(SongDTO song) {
        Optional<Song> existingSong = songRepository.findById(Long.parseLong(song.getId()));

        if (existingSong.isPresent())
            throw new DuplicateRecordException("Metadata for this ID already exists.");

        Song songEntity = songMapper.toEntity(song);
        return songRepository.save(songEntity);
    }

    @Override
    public SongDTO getSongById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Song with ID %d not found", id));
        return songMapper.toDTO(song);
    }

    @Override
    @Transactional
    public List<Long> deleteSongs(List<Long> ids) {
        List<Long> existingIds = ids.stream()
                .filter(songRepository::existsById)
                .collect(Collectors.toList());

        songRepository.deleteAllById(existingIds);

        return existingIds;
    }
}
