package com.epam.storageservice.mapper;

import com.epam.storageservice.dto.StorageDTO;
import com.epam.storageservice.entity.Storage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StorageMapper {

    StorageDTO toDTO(Storage storage);

    Storage toEntity(StorageDTO storageDTO);
}
