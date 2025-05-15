package com.epam.storageservice.controller;

import com.epam.storageservice.dto.StorageDTO;
import com.epam.storageservice.service.StorageService;
import com.epam.storageservice.util.validator.CsvValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/storages")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> createStorage(@RequestBody @Valid StorageDTO storageDTO) {
        return ResponseEntity.ok(Map.of("id", storageService.createStorage(storageDTO)));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StorageDTO>> getAllStorages() {
        return ResponseEntity.ok(storageService.getAllStorages());
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Long>>> deleteStorages(@RequestParam("id") String id) {
        List<Long> ids = CsvValidator.validateAndParseCsv(id);
        List<Long> deletedIds = storageService.deleteStorages(ids);
        return ResponseEntity.ok(Map.of("ids", deletedIds));
    }
}
