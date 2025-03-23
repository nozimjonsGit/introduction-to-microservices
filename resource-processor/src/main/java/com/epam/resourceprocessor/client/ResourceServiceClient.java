package com.epam.resourceprocessor.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ResourceServiceClient {

    private final RestTemplate restTemplate;

    @Value("${resource.service.url}")
    private String resourceServiceUrl;

    public byte[] getResourceData(Long resourceId) {
        String url = resourceServiceUrl + "/resources/" + resourceId;
        return restTemplate.getForObject(url, byte[].class);
    }
}
