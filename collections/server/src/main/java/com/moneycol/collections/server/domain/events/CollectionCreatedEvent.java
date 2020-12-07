package com.moneycol.collections.server.domain.events;

import com.moneycol.collections.server.domain.events.core.DomainEvent;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Introspected
public class CollectionCreatedEvent implements DomainEvent {

    private String collectionId;
    private String name;
    private String description;

    @Override
    public String eventId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Long occurredOn() {
        return Instant.now().toEpochMilli();
    }
}
