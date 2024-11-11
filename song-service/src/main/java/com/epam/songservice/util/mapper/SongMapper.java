package com.epam.songservice.util.mapper;

import com.epam.songservice.dto.SongDTO;
import com.epam.songservice.entity.Song;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SongMapper {

    SongDTO toDTO(Song song);

    Song toEntity(SongDTO songDTO);
}
