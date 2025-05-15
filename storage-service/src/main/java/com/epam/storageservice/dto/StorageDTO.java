package com.epam.storageservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StorageDTO implements Serializable {

    private Long id;

    @NotNull(message = "Storage type cannot be null")
    private StorageType type;

    @NotNull(message = "Bucket cannot be null")
    private String bucket;

    @NotNull(message = "Path cannot be null")
    private String path;
}
