package com.epam.resourceservice.config.properties;

import com.epam.resourceservice.entity.enums.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "storage")
@Data
public class DefaultStorageProperties {

    private Map<StorageType, StorageInfo> defaults = new EnumMap<>(StorageType.class);

    @Data
    public static class StorageInfo {
        private String bucket;
        private String path;
    }
}
