package com.moneycol.collections.server.domain.events.core;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class LocalGenericEventSubscriber<E extends DomainEvent> implements DomainEventSubscriber<E> {

    private List<DomainEvent> domainEvents = new ArrayList<>();
    private Class<E> clazz;

    @SuppressWarnings("unchecked")
    public LocalGenericEventSubscriber() {
        this.clazz = (Class<E>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Subscribe
    @Override
    public void subscribe(E domainEvent) {
        domainEvents.add(domainEvent);

        Class<?> subscribedToType = this.subscribedToEventType();

        if (domainEvent.getClass() == subscribedToType) {
            handleEvent(domainEvent);
        } else {
            log.debug("Event {} is not going to be handled by this subscriber {}",
                    domainEvent.getClass().getName(), clazz.getName());
        }
    }

    @Override
    public Class<E> subscribedToEventType() {
        return clazz;
    }

    public abstract void handleEvent(E domainEvent);
}
