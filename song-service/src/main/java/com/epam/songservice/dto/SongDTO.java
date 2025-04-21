package com.epam.songservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class SongDTO {
    @NotBlank(message = "Id must not be null")
    private String id;

    @NotBlank(message = "Song name must not be blank")
    private String name;

    @NotBlank(message = "Artist must not be blank")
    private String artist;

    @NotBlank(message = "Album must not be blank")
    private String album;

    @NotNull(message = "Duration must not be null")
    @Pattern(
            regexp = "^(([0-9]+:[0-5][0-9])|([0-9]+:[0-5][0-9]:[0-5][0-9]))$",
            message = "Duration must be in the format mm:ss or hh:mm:ss, with valid minutes and seconds.")
    private String duration;

    @NotNull(message = "Year must not be null")
    @Size(min = 4, max = 4, message = "Year must be in yyyy format")
    private String year;
}
