package com.epam.resourceservice.controller;

import com.epam.resourceservice.entity.Resource;
import com.epam.resourceservice.service.ResourceService;
import com.epam.resourceservice.util.validator.CustomValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping(consumes = "audio/mpeg", produces = "application/json")
    public ResponseEntity<Map<String, Long>> createResource(@RequestBody byte[] audio) {
        Resource savedResource = resourceService.createAndProcessResource(audio);
        return ResponseEntity.ok(Map.of("id", savedResource.getId()));
    }

    @GetMapping(value = "/{id}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getResource(@PathVariable String id) {
        CustomValidator.validateId(id);
        byte[] file = resourceService.getResourceFileById(Long.parseLong(id));
        return ResponseEntity.ok(file);
    }

    @DeleteMapping(produces = "application/json")
    public ResponseEntity<Map<String, List<Long>>> deleteResources(@RequestParam("id") String id) {
        List<Long> ids = CustomValidator.validateAndParseCsv(id);
        List<Long> deletedIds = resourceService.deleteResources(ids);
        return ResponseEntity.ok(Map.of("ids", deletedIds));
    }
}

