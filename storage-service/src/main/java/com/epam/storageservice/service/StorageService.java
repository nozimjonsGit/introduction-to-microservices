package com.epam.storageservice.service;

import com.epam.storageservice.dto.StorageDTO;

import java.util.List;

public interface StorageService {
     Long createStorage(StorageDTO storageDTO);

     List<StorageDTO> getAllStorages();

    List<Long> deleteStorages(List<Long> ids);
}
