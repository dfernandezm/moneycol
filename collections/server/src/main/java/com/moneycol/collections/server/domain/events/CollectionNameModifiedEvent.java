package com.moneycol.collections.server.domain.events;

import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.events.core.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public class CollectionNameModifiedEvent implements DomainEvent {

    private Long occurredOn;
    private String newCollectionName;
    private CollectionId collectionId;

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
