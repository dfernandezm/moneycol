package com.moneycol.collections.server.events;

import com.moneycol.collections.server.FirestoreHelper;
import com.moneycol.collections.server.TestHelper;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.events.CollectionCreatedEvent;
import com.moneycol.collections.server.domain.events.EventStore;
import com.moneycol.collections.server.domain.events.core.DomainEvent;
import com.moneycol.collections.server.infrastructure.event.FirestoreEventStore;
import com.moneycol.collections.server.infrastructure.repository.FirestoreProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class FirestoreEventStoreTest {

    private static FirestoreProvider firestoreProvider;

    @BeforeAll
    public static void setup() {
        firestoreProvider = FirestoreHelper.initContainer();

    }

    @AfterEach
    public void cleanup() {
        FirestoreHelper.deleteAllCollections();
    }

    @Test
    public void createsADomainEventTest() {
        String collectionName = "collectionName";
        String collectionDescription = "Collection Description";
        CollectionCreatedEvent collectionCreatedEvent = CollectionCreatedEvent.builder()
                                                        .collectionId(CollectionId.randomId())
                                                        .name(collectionName)
                                                        .description(collectionDescription)
                                                        .build();

        EventStore eventStore = new FirestoreEventStore(firestoreProvider);
        eventStore.store(collectionCreatedEvent);

        TestHelper.delayMillisecond(250);

        String eventId = collectionCreatedEvent.eventId();
        Map<String, Object> eventData = FirestoreHelper.findEventData("events", eventId);

        assertThat(eventData.get("name")).isEqualTo(collectionName);
    }

    @Test
    public void createdEventUsesEventId() {
        CollectionCreatedEvent collectionCreatedEvent = TestHelper.aCollectionCreatedEvent();

        FakeEventStore eventStore = new FakeEventStore();
        eventStore.store(collectionCreatedEvent);

        String eventId = collectionCreatedEvent.eventId();
        DomainEvent domainEvent = eventStore.byId(eventId);

        assertThat(domainEvent).isNotNull();
    }

    private static class FakeEventStore implements EventStore {

        private Map<String, DomainEvent> eventsMap = new HashMap<>();

        @Override
        public void store(DomainEvent domainEvent) {
            eventsMap.put(domainEvent.eventId(), domainEvent);
        }

        private DomainEvent byId(String eventId) {
            return eventsMap.get(eventId);
        }
    }
}
