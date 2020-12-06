package com.moneycol.collections.server.domain.events;

import com.google.common.base.MoreObjects;
import com.moneycol.collections.server.domain.events.core.DomainEvent;
import com.moneycol.collections.server.domain.events.core.LocalEventSubscriber;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DefaultDomainEventSubscriber extends LocalEventSubscriber<DomainEvent> {

    @Override
    public void handleEvent(DomainEvent domainEvent) {
        log.info("Handled event {}", MoreObjects.toStringHelper(domainEvent));
    }

    @Override
    public Class<DomainEvent> subscribedToEventType() {
        return DomainEvent.class;
    }
}
