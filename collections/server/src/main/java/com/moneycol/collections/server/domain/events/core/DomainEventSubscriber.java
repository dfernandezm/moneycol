package com.moneycol.collections.server.domain.events.core;

public interface DomainEventSubscriber<T extends DomainEvent> {
    public void subscribe(T domainEvent);
    public Class<T> subscribedToEventType();
}
