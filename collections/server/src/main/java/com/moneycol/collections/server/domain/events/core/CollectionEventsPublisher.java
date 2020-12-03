package com.moneycol.collections.server.domain.events.core;

import com.moneycol.collections.server.domain.events.CollectionNameModifiedEvent;

public class CollectionEventsPublisher extends LocalEventPublisher {

    public void publish(CollectionNameModifiedEvent domainEvent) {
        super.publish(domainEvent);
    }
}
