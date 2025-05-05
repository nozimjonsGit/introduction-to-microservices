package com.epam.e2etests.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongMetadataDTO {
    private Long id;
    private String name;
    private String artist;
    private String album;
    private String duration;
    private String year;
}
