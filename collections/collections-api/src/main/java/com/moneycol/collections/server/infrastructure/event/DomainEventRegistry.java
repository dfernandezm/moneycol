package com.moneycol.collections.server.infrastructure.event;

import com.moneycol.collections.server.domain.events.DomainEventStoringSubscriber;
import com.moneycol.collections.server.domain.events.core.DomainEvent;
import com.moneycol.collections.server.domain.events.core.DomainEventPublisher;
import com.moneycol.collections.server.domain.events.core.DomainEventSubscriber;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of an eventBus to publish/subscribe to domain events
 * By default uses a pre-made {@link DomainEventPublisher} and {@link DomainEventSubscriber}
 *
 * Events may need to be subscribed to at application startup or use a dedicated ApplicationService for it
 */
@Slf4j
@Singleton
public class DomainEventRegistry {

    private DomainEventPublisher<DomainEvent> publisher;
    private List<DomainEventSubscriber<DomainEvent>> subscribers = new ArrayList<>();

    @Inject
    public DomainEventRegistry(
            DomainEventPublisher<DomainEvent> defaultDomainEventPublisher,
            DomainEventSubscriber<DomainEvent> defaultDomainEventSubscriber,
            DomainEventStoringSubscriber storingDomainEventSubscriber) {
        this.publisher = defaultDomainEventPublisher;
        subscribers.add(defaultDomainEventSubscriber);
        subscribers.add(storingDomainEventSubscriber);
        subscribeToAll();
    }

    public void publish(DomainEvent domainEvent) {
        publisher.publish(domainEvent);
    }

    public void addSubscriber(DomainEventSubscriber<DomainEvent> domainEventDomainEventSubscriber) {
        publisher.register(domainEventDomainEventSubscriber);
    }

    private void subscribe(DomainEventSubscriber<DomainEvent> domainEventDomainEventSubscriber) {
        publisher.register(domainEventDomainEventSubscriber);
    }

    /**
     * Subscribe to all
     *
     */
    private void subscribeToAll() {
        subscribers.forEach(subscriber -> {
            publisher.register(subscriber);
        });
    }
}
