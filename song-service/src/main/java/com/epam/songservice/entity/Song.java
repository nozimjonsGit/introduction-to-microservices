package com.epam.songservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "songs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Song {
    @Id
    private Long id;

    private String name;

    private String artist;

    private String album;

    private String duration;

    private String year;
}
