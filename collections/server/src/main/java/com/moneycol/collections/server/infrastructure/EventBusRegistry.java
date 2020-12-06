package com.moneycol.collections.server.infrastructure;

import com.moneycol.collections.server.domain.events.core.DomainEvent;
import com.moneycol.collections.server.domain.events.core.DomainEventPublisher;
import com.moneycol.collections.server.domain.events.core.DomainEventSubscriber;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class EventBusRegistry {

    private DomainEventPublisher<DomainEvent> publisher;
    private DomainEventSubscriber<DomainEvent> subscriber;

    @Inject
    public EventBusRegistry(
            DomainEventPublisher<DomainEvent> defaultDomainEventPublisher,
            DomainEventSubscriber<DomainEvent> defaultDomainEventSubscriber) {
        this.publisher = defaultDomainEventPublisher;
        this.subscriber = defaultDomainEventSubscriber;
        subscribe(this.subscriber);
    }

    public void publish(DomainEvent domainEvent) {
        publisher.publish(domainEvent);
    }

    public void subscribe(DomainEventSubscriber<DomainEvent> domainEventDomainEventSubscriber) {
        publisher.register(domainEventDomainEventSubscriber);
    }
}
