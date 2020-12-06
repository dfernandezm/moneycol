package com.moneycol.collections.server.domain.events.core;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class LocalEventSubscriber<T extends DomainEvent> implements DomainEventSubscriber<T> {

    private List<DomainEvent> domainEvents = new ArrayList<>();

    @Subscribe
    @Override
    public final void subscribe(T domainEvent) {
        domainEvents.add(domainEvent);

        Class<?> subscribedToType = this.subscribedToEventType();

        //TODO: not valid, need to check types better
        if (domainEvent.getClass().isAssignableFrom(subscribedToType)) {
            handleEvent(domainEvent);
        }
    }

    public abstract void handleEvent(T domainEvent);
}
