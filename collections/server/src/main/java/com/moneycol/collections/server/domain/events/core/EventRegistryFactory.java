package com.moneycol.collections.server.domain.events.core;


import com.moneycol.collections.server.domain.events.DefaultDomainEventSubscriber;
import com.moneycol.collections.server.domain.events.EventStore;
import com.moneycol.collections.server.infrastructure.event.FirestoreEventStore;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

/**
 *  Micronaut Factory for Local EventBus implementation
 */
@Factory
public class EventRegistryFactory {

    @Bean
    public DomainEventSubscriber<DomainEvent> defaultDomainEventSubscriber() {
        return new DefaultDomainEventSubscriber();
    }

    @Bean
    public DomainEventPublisher<DomainEvent> defaultDomainEventPublisher() {
        return new LocalEventPublisher<DomainEvent>();
    }

    @Bean
    public EventStore eventStore(FirestoreEventStore firestoreEventStore) {
        return firestoreEventStore;
    }
}
