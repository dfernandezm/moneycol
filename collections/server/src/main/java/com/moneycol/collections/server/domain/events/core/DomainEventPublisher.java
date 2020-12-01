package com.moneycol.collections.server.domain.events.core;

public interface DomainEventPublisher {
    void publish(DomainEvent domainEvent);
    void register(DomainEventListener eventListener);
}
