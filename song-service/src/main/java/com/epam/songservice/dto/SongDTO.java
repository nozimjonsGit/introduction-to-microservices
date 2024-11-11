package com.epam.songservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SongDTO {
    @NotBlank(message = "Song name must not be blank")
    private String name;

    @NotBlank(message = "Artist must not be blank")
    private String artist;

    @NotBlank(message = "Album must not be blank")
    private String album;

    @NotNull(message = "Length must not be null")
    private String length;

    @NotNull(message = "Resource ID must not be null")
    private Long resourceId;

    private String year;
}
