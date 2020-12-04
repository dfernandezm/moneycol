package com.moneycol.collections.server.events;

import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.events.CollectionNameModifiedEvent;
import com.moneycol.collections.server.domain.events.core.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionEventsPublisherTest {

    @Test
    public void publishesKnownEventTest() {

        DomainEventPublisher<CollectionNameModifiedEvent> collectionEventsPublisher =
                new LocalGenericEventPublisher<>();

        CollectionId collectionId = CollectionId.of(CollectionId.randomId());
        String newCollectionName = "newCollectionName";

        CollectionNameModifiedEvent collectionNameModifiedEvent =
                CollectionNameModifiedEvent.builder()
                        .newName(newCollectionName)
                        .collectionId(collectionId)
                        .build();

        DomainEventSubscriber<CollectionNameModifiedEvent> listener =
                new LocalGenericEventSubscriber<CollectionNameModifiedEvent>() {
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
    public void multipleDeadEventListenersDontHandleSameEventMoreThanOnce() {
        LocalEventPublisher collectionEventsPublisher = new LocalEventPublisher(new LocalDeadEventListener());

        //TODO: This should work by not listening on that event and forward to dead letter
//        collectionEventsPublisher.register(new LocalEventSubscriber<DomainEvent>() {
//            @Override
//            public void listen(DomainEvent domainEvent) {
//               System.out.println("No domain event should be handled");
//            }
//        });

        collectionEventsPublisher.register(new CollectionEventSubscriber());

        collectionEventsPublisher.publish(aDomainEvent());
        collectionEventsPublisher.publish(aDomainEvent());

        assertThat(collectionEventsPublisher.deadEventCount()).isEqualTo(2);

    }


    @Test
    public void emptyDeadEventCountTest() {
        LocalEventPublisher collectionEventsPublisher = new LocalEventPublisher(new LocalDeadEventListener());

        assertThat(collectionEventsPublisher.deadEventCount()).isEqualTo(0);
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
