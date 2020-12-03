package com.moneycol.collections.server.domain.events;

import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.events.core.DomainEvent;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data

public class CollectionNameModifiedEvent implements DomainEvent {

    private Long occurredOn;
    private String newCollectionName;
    private CollectionId collectionId;

    @Builder
    public CollectionNameModifiedEvent(String newName, CollectionId collectionId) {
        this.occurredOn = Instant.now().toEpochMilli();
        this.newCollectionName = newName;
        this.collectionId = collectionId;
    }

    @Override
    public String eventId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Long occurredOn() {
        return occurredOn;
    }
}
