package com.moneycol.collections.server.domain.events;

import com.moneycol.collections.server.domain.events.core.DomainEvent;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
@Introspected
public class CollectionCreatedEvent implements DomainEvent {

    @Builder.Default
    String eventId = UUID.randomUUID().toString();

    @Builder.Default
    Long occurredOn = Instant.now().toEpochMilli();

    String collectionId;
    String name;
    String description;

    @Override
    public String eventId() {
        return eventId;
    }

    @Override
    public Long occurredOn() {
        return occurredOn;
    }
}
