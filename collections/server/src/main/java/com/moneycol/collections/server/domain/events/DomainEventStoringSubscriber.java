package com.moneycol.collections.server.domain.events;

import com.google.common.base.MoreObjects;
import com.moneycol.collections.server.domain.events.core.DomainEvent;
import com.moneycol.collections.server.domain.events.core.LocalEventSubscriber;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;


@Slf4j
public class DomainEventStoringSubscriber extends LocalEventSubscriber<DomainEvent> {

    private EventStore eventStore;

    @Inject
    public DomainEventStoringSubscriber(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void handleEvent(DomainEvent domainEvent) {
        log.info("Storing event {}", MoreObjects.toStringHelper(domainEvent));
        eventStore.store(domainEvent);
    }

    @Override
    public Class<DomainEvent> subscribedToEventType() {
        return DomainEvent.class;
    }
}
