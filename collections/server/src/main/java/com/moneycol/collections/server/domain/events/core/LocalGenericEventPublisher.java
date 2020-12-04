package com.moneycol.collections.server.domain.events.core;

import com.google.common.eventbus.EventBus;
import org.assertj.core.util.VisibleForTesting;

import java.lang.reflect.ParameterizedType;

public class LocalGenericEventPublisher<E extends DomainEvent> implements DomainEventPublisher<E> {

    private static final String DEFAULT_LOCAL_EVENTBUS_NAME = "default";
    private EventBus eventBus;
    private LocalDeadEventListener deadEventListener;
    private Class<E> clazz;

    public LocalGenericEventPublisher() {
        this(DEFAULT_LOCAL_EVENTBUS_NAME);
    }

    public LocalGenericEventPublisher(LocalDeadEventListener deadEventListener) {
        eventBus = new EventBus(DEFAULT_LOCAL_EVENTBUS_NAME);

        //TODO: for this to work needs to extend, not only implement
        // https://stackoverflow.com/questions/3403909/get-generic-type-of-class-at-runtime
        this.clazz = (Class<E>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.deadEventListener = deadEventListener;
        registerDeadEventListener();
    }

    public LocalGenericEventPublisher(String eventPublisherName) {
        eventBus = new EventBus(eventPublisherName);
        this.clazz = (Class<E>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.deadEventListener = new LocalDeadEventListener();
        registerDeadEventListener();
    }

    private void registerDeadEventListener() {
        eventBus.register(deadEventListener);
    }

    @Override
    public void publish(E domainEvent) {
        eventBus.post(domainEvent);
    }

    public void register(DomainEventSubscriber<E> eventSubscriber) {
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
