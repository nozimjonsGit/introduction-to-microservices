package com.epam.resourceservice.dto;

import com.epam.resourceservice.entity.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResourceStorageDTO implements Serializable {
    private Long id;

    private StorageType type;

    private String bucket;

    private String path;
}
