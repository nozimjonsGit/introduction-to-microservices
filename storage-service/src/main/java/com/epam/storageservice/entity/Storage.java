package com.epam.storageservice.entity;

import com.epam.storageservice.dto.StorageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "storages")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private StorageType type;

    @Column(nullable = false, unique = true)
    private String bucket;

    @Column(nullable = false)
    private String path;
}
