package com.moneycol.collections.server.domain.events.core;

public interface DomainEvent {
    String eventId();
    String eventName();
    Long occurredOn();
}
