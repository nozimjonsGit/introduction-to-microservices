package com.epam.songservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "songs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Song {
    @Id
    private Long id;

    private String name;

    private String artist;

    private String album;

    private String duration;

    private String year;
}
