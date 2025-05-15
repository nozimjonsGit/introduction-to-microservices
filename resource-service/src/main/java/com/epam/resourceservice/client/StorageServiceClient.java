package com.epam.resourceservice.client;

import com.epam.resourceservice.config.properties.DefaultStorageProperties;
import com.epam.resourceservice.dto.ResourceStorageDTO;
import com.epam.resourceservice.entity.enums.StorageType;
import com.epam.resourceservice.exception.custom.StorageServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StorageServiceClient {

    @Value("${storage.service.url}")
    private static String STORAGE_SERVICE_URL;

    private static final String RETRY_CONFIG_NAME = "storageServiceRetry";
    private static final String CIRCUIT_BREAKER_CONFIG_NAME = "storageServiceCircuitBreaker";

    private final RestTemplate restTemplate;
    private final DefaultStorageProperties defaultProps;

    @Retry(name = RETRY_CONFIG_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_CONFIG_NAME, fallbackMethod = "fallbackGetStorage")
    public ResourceStorageDTO getStorage(StorageType type) {
        ResponseEntity<List<ResourceStorageDTO>> response = restTemplate.exchange(
                STORAGE_SERVICE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new StorageServiceException(
                    "Unexpected response from Storage Service: " + response.getStatusCode()
            );
        }

        return response.getBody().stream()
                .filter(dto -> type.equals(dto.getType()))
                .findFirst()
                .orElseGet(() -> getDefault(type));
    }

    @SuppressWarnings("unused")
    private ResourceStorageDTO fallbackGetStorage(StorageType type, Throwable ex) {
        log.warn("Fallback for getStorage({}) after retries/CircuitBreaker: {}", type, ex.getMessage());
        return getDefault(type);
    }

    private ResourceStorageDTO getDefault(StorageType type) {
        var info = defaultProps.getDefaults().get(type);
        if (info == null) {
            throw new IllegalStateException("No default configured for " + type.name());
        }
        return new ResourceStorageDTO(0L, type, info.getBucket(), info.getPath());
    }
}
