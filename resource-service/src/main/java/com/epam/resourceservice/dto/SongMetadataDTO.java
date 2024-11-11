package com.epam.resourceservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongMetadataDTO {
    private String name;
    private String artist;
    private String album;
    private String length;
    private Long resourceId;
    private String year;

    public SongMetadataDTO(String name, String artist, String album, String length, String year) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.length = length;
        this.year = year;
    }
}
