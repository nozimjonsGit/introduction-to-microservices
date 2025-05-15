package com.epam.resourceservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_key", nullable = false, unique = true)
    private String fileKey;

    @Column(nullable = false)
    private String bucket;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String path;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp(source = SourceType.VM)
    private LocalDateTime createdAt;
}
