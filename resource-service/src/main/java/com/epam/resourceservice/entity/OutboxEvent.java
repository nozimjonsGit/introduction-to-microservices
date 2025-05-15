package com.epam.resourceservice.entity;

import com.epam.resourceservice.entity.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outbox_event")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Long resourceId;

    private String bucket;

    private String path;

    private String fileKey;

    @Column(columnDefinition = "bytea")
    private byte[] resourceData;
}
