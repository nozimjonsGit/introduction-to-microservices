package com.epam.resourceprocessor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongMetadataDTO {
    private String id;
    private String name;
    private String artist;
    private String album;
    private String duration;
    private String year;

    public SongMetadataDTO(String name, String artist, String album, String duration, String year) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.year = year;
    }
}
