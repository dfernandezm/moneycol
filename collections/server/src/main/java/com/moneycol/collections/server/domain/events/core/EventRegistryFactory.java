package com.moneycol.collections.server.domain.events.core;


import com.moneycol.collections.server.domain.events.DefaultDomainEventSubscriber;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

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
}
