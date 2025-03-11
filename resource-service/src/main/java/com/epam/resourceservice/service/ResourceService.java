package com.epam.resourceservice.service;

import com.epam.resourceservice.entity.Resource;

import java.util.List;

public interface ResourceService {
    Resource createAndProcessResource(byte[] audio);
    byte[] getResourceFileById(Long id);
    List<Long> deleteResources(List<Long> ids);
}
