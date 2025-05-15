package com.epam.resourceservice.repository;

import com.epam.resourceservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
}
