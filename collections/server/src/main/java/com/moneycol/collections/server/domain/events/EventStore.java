package com.moneycol.collections.server.domain.events;

import com.moneycol.collections.server.domain.events.core.DomainEvent;

public interface EventStore {
    void store(DomainEvent domainEvent);
}
