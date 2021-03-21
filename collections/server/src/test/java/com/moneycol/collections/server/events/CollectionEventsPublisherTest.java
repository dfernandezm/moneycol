package com.moneycol.collections.server.events;

import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.events.CollectionCreatedEvent;
import com.moneycol.collections.server.domain.events.DomainEventStoringSubscriber;
import com.moneycol.collections.server.domain.events.core.DomainEvent;
import com.moneycol.collections.server.domain.events.core.DomainEventPublisher;
import com.moneycol.collections.server.domain.events.core.DomainEventSubscriber;
import com.moneycol.collections.server.domain.events.core.LocalDeadEventListener;
import com.moneycol.collections.server.domain.events.core.LocalEventPublisher;
import com.moneycol.collections.server.domain.events.core.LocalEventSubscriber;
import com.moneycol.collections.server.infrastructure.event.DomainEventRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionEventsPublisherTest {

    @Test
    public void publishesKnownEventTest() {

        DomainEventPublisher<CollectionCreatedEvent> collectionEventsPublisher =
                new LocalEventPublisher<>();

        CollectionId collectionId = CollectionId.of(CollectionId.randomId());
        String newCollectionName = "newCollectionName";


        CollectionCreatedEvent collectionCreatedEvent =
                CollectionCreatedEvent.builder()
                        .name(newCollectionName)
                        .collectionId(collectionId.id())
                        .build();

        DomainEventSubscriber<CollectionCreatedEvent> listener =
                new LocalEventSubscriber<CollectionCreatedEvent>() {
                    @Override
                    public Class<CollectionCreatedEvent> subscribedToEventType() {
                        return CollectionCreatedEvent.class;
                    }

                    @Override
                    public void handleEvent(CollectionCreatedEvent domainEvent) {
                        assertThat(domainEvent.getCollectionId()).isEqualTo(collectionId.id());
                        assertThat(domainEvent.getName()).isEqualTo(newCollectionName);
                    }
        };

        collectionEventsPublisher.register(listener);
        collectionEventsPublisher.publish(collectionCreatedEvent);
    }

    @Test
    public void unknownEventIsHandledByDeadEventListenerTest() {
        LocalEventPublisher collectionEventsPublisher = new LocalEventPublisher(new LocalDeadEventListener());
        collectionEventsPublisher.publish(aDomainEvent());

        assertThat(collectionEventsPublisher.deadEventCount()).isEqualTo(1);
    }

    @Test
    public void moreThanOneUnknownEventsAreHandledTest() {
        LocalEventPublisher collectionEventsPublisher = new LocalEventPublisher(new LocalDeadEventListener());
        collectionEventsPublisher.publish(aDomainEvent());
        collectionEventsPublisher.publish(anotherDomainEvent());

        assertThat(collectionEventsPublisher.deadEventCount()).isEqualTo(2);
    }

    @Test
    public void emptyDeadEventCountTest() {
        LocalEventPublisher collectionEventsPublisher = new LocalEventPublisher(new LocalDeadEventListener());
        assertThat(collectionEventsPublisher.deadEventCount()).isEqualTo(0);
    }

    @Test
    public void handleEventIsCalled() {
        final boolean[] called = {false};
        DomainEventRegistry eventBusRegistry = new DomainEventRegistry(new LocalEventPublisher<>(),
                new LocalEventSubscriber<DomainEvent>() {
                    @Override
                    public void handleEvent(DomainEvent domainEvent) {
                        called[0] = true;
                    }

                    @Override
                    public Class<DomainEvent> subscribedToEventType() {
                        return DomainEvent.class;
                    }
                }, Mockito.mock(DomainEventStoringSubscriber.class));

        eventBusRegistry.publish(aDomainEvent());
        assertThat(called[0]).isTrue();

    }

    private DomainEvent aDomainEvent() {
        return new DomainEvent() {
            @Override
            public String eventId() {
                return "uuid1";
            }

            @Override
            public String eventName() {
                return "ADomainEvent";
            }

            @Override
            public Long occurredOn() {
                return Instant.now().toEpochMilli();
            }
        };
    }

    private DomainEvent anotherDomainEvent() {
        return new DomainEvent() {
            @Override
            public String eventId() {
                return "uuid2";
            }

            @Override
            public String eventName() {
                return "AnotherDomainEvent";
            }

            @Override
            public Long occurredOn() {
                return Instant.now().toEpochMilli();
            }
        };
    }
}
