package com.moneycol.collections.server.infrastructure.event;

import com.moneycol.collections.server.domain.events.EventStore;
import com.moneycol.collections.server.domain.events.core.DomainEvent;

public class FirestoreEventStore implements EventStore {
    @Override
    public void store(DomainEvent domainEvent) {
        //store event in firestore
    }
}
