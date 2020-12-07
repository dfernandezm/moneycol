package com.moneycol.collections.server.infrastructure;

import com.moneycol.collections.server.domain.events.core.DomainEvent;
import com.moneycol.collections.server.domain.events.core.DomainEventPublisher;
import com.moneycol.collections.server.domain.events.core.DomainEventSubscriber;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of an eventBus to publish/subscribe to domain events
 *
 * By default uses a pre-made {@link DomainEventPublisher} and {@link DomainEventSubscriber}
 */

//TODO: need to subscribe to multiple events on startup??
// or include EventStore in a global ApplicationService manager
@Slf4j
@Singleton
public class DomainEventRegistry {

    private DomainEventPublisher<DomainEvent> publisher;
    private List<DomainEventSubscriber<DomainEvent>> subscribers = new ArrayList<>();

    @Inject
    public DomainEventRegistry(
            DomainEventPublisher<DomainEvent> defaultDomainEventPublisher,
            DomainEventSubscriber<DomainEvent> defaultDomainEventSubscriber) {
        this.publisher = defaultDomainEventPublisher;
        subscribers.add(defaultDomainEventSubscriber);
        addSubscriber(defaultDomainEventSubscriber);
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
