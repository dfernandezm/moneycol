package com.moneycol.collections.server.domain.events.core;

import com.google.common.eventbus.EventBus;

public class LocalEventPublisher implements DomainEventPublisher {

    private EventBus eventBus;

    public LocalEventPublisher(String eventPublisherName) {
        eventBus = new EventBus(eventPublisherName);
    }

    @Override
    public void publish(DomainEvent domainEvent) {
        eventBus.post(domainEvent);
    }

    public void register(DomainEventListener eventListener) {
        eventBus.register(eventListener);
    }

    public void unregister(DomainEventListener domainEventListener) {
        eventBus.unregister(domainEventListener);
    }
}
