package com.epam.resourceprocessor.service;

public interface ResourceProcessor {
    void process(Long resourceId, String traceId, String spanId);
}
