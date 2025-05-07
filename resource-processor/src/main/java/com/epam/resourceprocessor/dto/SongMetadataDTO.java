package com.epam.resourceprocessor.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SongMetadataDTO implements Serializable {
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
