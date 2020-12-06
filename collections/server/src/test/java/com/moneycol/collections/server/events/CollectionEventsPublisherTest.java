package com.moneycol.collections.server.events;

import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.events.CollectionNameModifiedEvent;
import com.moneycol.collections.server.domain.events.core.DomainEvent;
import com.moneycol.collections.server.domain.events.core.DomainEventPublisher;
import com.moneycol.collections.server.domain.events.core.DomainEventSubscriber;
import com.moneycol.collections.server.domain.events.core.LocalDeadEventListener;
import com.moneycol.collections.server.domain.events.core.LocalEventPublisher;
import com.moneycol.collections.server.domain.events.core.LocalEventSubscriber;
import com.moneycol.collections.server.infrastructure.EventBusRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionEventsPublisherTest {

    @Test
    public void publishesKnownEventTest() {

        DomainEventPublisher<CollectionNameModifiedEvent> collectionEventsPublisher =
                new LocalEventPublisher<>();

        CollectionId collectionId = CollectionId.of(CollectionId.randomId());
        String newCollectionName = "newCollectionName";

        CollectionNameModifiedEvent collectionNameModifiedEvent =
                CollectionNameModifiedEvent.builder()
                        .newName(newCollectionName)
                        .collectionId(collectionId)
                        .build();

        DomainEventSubscriber<CollectionNameModifiedEvent> listener =
                new LocalEventSubscriber<CollectionNameModifiedEvent>() {
                    @Override
                    public Class<CollectionNameModifiedEvent> subscribedToEventType() {
                        return CollectionNameModifiedEvent.class;
                    }

                    @Override
                    public void handleEvent(CollectionNameModifiedEvent domainEvent) {
                        assertThat(domainEvent.getCollectionId()).isEqualTo(collectionId);
                        assertThat(domainEvent.getNewCollectionName()).isEqualTo(newCollectionName);
                    }
        };

        collectionEventsPublisher.register(listener);
        collectionEventsPublisher.publish(collectionNameModifiedEvent);
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
        EventBusRegistry eventBusRegistry = new EventBusRegistry(new LocalEventPublisher<>(),
                new LocalEventSubscriber<DomainEvent>() {
                    @Override
                    public void handleEvent(DomainEvent domainEvent) {
                        called[0] = true;
                    }

                    @Override
                    public Class<DomainEvent> subscribedToEventType() {
                        return DomainEvent.class;
                    }
                });

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
            public Long occurredOn() {
                return Instant.now().toEpochMilli();
            }
        };
    }
}
