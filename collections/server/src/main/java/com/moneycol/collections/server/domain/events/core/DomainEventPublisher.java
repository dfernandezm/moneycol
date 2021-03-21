package com.moneycol.collections.server.domain.events.core;

public interface DomainEventPublisher<T extends DomainEvent> {
    void publish(T domainEvent);
    void register(DomainEventSubscriber<T> eventSubscriber);
}
