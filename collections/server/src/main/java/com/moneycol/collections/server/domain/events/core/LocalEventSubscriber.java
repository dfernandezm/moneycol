package com.moneycol.collections.server.domain.events.core;

import com.google.common.eventbus.Subscribe;
import java.util.List;

public abstract class LocalEventSubscriber<E extends DomainEvent> implements DomainEventListener<E> {

    private List<E> domainEvents;

    @Subscribe
    @Override
    public void subscribe(E domainEvent) {
        domainEvents.add(domainEvent);
        listen(domainEvent);
    }

    public abstract void listen(E domainEvent);
}
