package com.moneycol.collections.server.domain.events.core;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class LocalEventSubscriber implements DomainEventSubscriber<DomainEvent> {

    private List<DomainEvent> domainEvents = new ArrayList<>();

    @Subscribe
    @Override
    public void subscribe(DomainEvent domainEvent) {
        domainEvents.add(domainEvent);
        //listen(domainEvent);
    }

    @Override
    public Class<DomainEvent> subscribedToEventType() {
        return null;
    }

    public abstract void listen(DomainEvent domainEvent);
}
