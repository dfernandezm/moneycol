package com.moneycol.collections.server.domain.events.core;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class LocalEventSubscriber<E extends DomainEvent> implements DomainEventListener<E> {

    private List<E> domainEvents = new ArrayList<>();

    @Subscribe
    @Override
    public void subscribe(E domainEvent) {
        domainEvents.add(domainEvent);
        listen(domainEvent);
    }

    public abstract void listen(E domainEvent);

    @Subscribe
    public void handleDeadEvent(DeadEvent deadEvent) {
        log.info("Dead Event in local subscriber");
    }
}
