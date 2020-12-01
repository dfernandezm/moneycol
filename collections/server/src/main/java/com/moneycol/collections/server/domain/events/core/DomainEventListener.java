package com.moneycol.collections.server.domain.events.core;

public interface DomainEventListener<T extends DomainEvent> {
    void subscribe(T domainEvent);
}
