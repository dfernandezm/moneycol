package com.moneycol.collections.server.domain.events.core;

import com.google.common.eventbus.EventBus;
import org.assertj.core.util.VisibleForTesting;

public class LocalEventPublisher<T extends DomainEvent> implements DomainEventPublisher<T> {

    private static final String DEFAULT_LOCAL_EVENTBUS_NAME = "default";
    private EventBus eventBus;
    private LocalDeadEventListener deadEventListener;

    public LocalEventPublisher() {
        this(DEFAULT_LOCAL_EVENTBUS_NAME);
    }

    public LocalEventPublisher(LocalDeadEventListener deadEventListener) {
        eventBus = new EventBus(DEFAULT_LOCAL_EVENTBUS_NAME);
        this.deadEventListener = deadEventListener;
        registerDeadEventListener();
    }

    public LocalEventPublisher(String eventPublisherName) {
        eventBus = new EventBus(eventPublisherName);
        this.deadEventListener = new LocalDeadEventListener();
        registerDeadEventListener();
    }

    private void registerDeadEventListener() {
        eventBus.register(deadEventListener);
    }

    @Override
    public void publish(DomainEvent domainEvent) {
        eventBus.post(domainEvent);
    }

    public void register(DomainEventSubscriber eventSubscriber) {
        eventBus.register(eventSubscriber);
    }

    @VisibleForTesting
    public int deadEventCount() {
        if (deadEventListener == null) {
            return -1;
        } else {
            return deadEventListener.deadEventCount();
        }
    }
}
