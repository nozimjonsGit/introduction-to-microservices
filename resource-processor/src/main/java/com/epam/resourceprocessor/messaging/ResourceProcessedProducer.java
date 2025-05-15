package com.epam.resourceprocessor.messaging;

public interface ResourceProcessedProducer {

    void publishProcessed(Long resourceId);
}
