package com.moneycol.collections.server.domain.events.core;

public interface DomainEventSubscriber<T extends DomainEvent> {
     void subscribe(T domainEvent);
     Class<T> subscribedToEventType();
}
