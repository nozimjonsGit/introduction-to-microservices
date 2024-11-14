package com.epam.songservice.controller;

import com.epam.songservice.dto.SongDTO;
import com.epam.songservice.entity.Song;
import com.epam.songservice.service.SongService;
import com.epam.songservice.util.validator.CustomValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Long>> createSong(@Valid @RequestBody SongDTO song) {
        Song createdSong = songService.createSong(song);
        return ResponseEntity.ok(Map.of("id", createdSong.getId()));
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<SongDTO> getSongById(@PathVariable Long id) {
        return ResponseEntity.ok(songService.getSongById(id));
    }

    @DeleteMapping(produces = "application/json")
    public ResponseEntity<Map<String, List<Long>>> deleteSongs(@RequestParam("id") String id) {
        List<Long> ids = CustomValidator.validateAndParseCsv(id);
        List<Long> deletedIds = songService.deleteSongs(ids);
        return ResponseEntity.ok(Map.of("ids", deletedIds));
    }
}
