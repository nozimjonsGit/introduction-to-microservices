package com.epam.storageservice.service.impl;

import com.epam.storageservice.dto.StorageDTO;
import com.epam.storageservice.entity.Storage;
import com.epam.storageservice.mapper.StorageMapper;
import com.epam.storageservice.repository.StorageRepository;
import com.epam.storageservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageRepository storageRepository;
    private final StorageMapper storageMapper;


    @Override
    @Transactional
    public Long createStorage(StorageDTO storageDTO) {
        Storage storage = storageMapper.toEntity(storageDTO);
        Storage savedStorage = storageRepository.save(storage);
        return savedStorage.getId();
    }

    @Override
    public List<StorageDTO> getAllStorages() {
        return storageRepository.findAll().stream()
                .map(storageMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Long> deleteStorages(List<Long> ids) {
        List<Long> existingIds = ids.stream()
                .filter(storageRepository::existsById)
                .collect(Collectors.toList());

        storageRepository.deleteAllById(existingIds);

        return existingIds;
    }
}
