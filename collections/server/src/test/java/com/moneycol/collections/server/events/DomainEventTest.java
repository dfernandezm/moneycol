package com.moneycol.collections.server.events;

import com.moneycol.collections.server.TestHelper;
import com.moneycol.collections.server.domain.events.CollectionCreatedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainEventTest {

    @Test
    public void occurredOnIsConsistent() {
        // Given: an event
        CollectionCreatedEvent collectionCreatedEvent = TestHelper.aCollectionCreatedEvent();

        // When: getting an occurredOn date now
        Long occurredOnFirst = collectionCreatedEvent.occurredOn();

        TestHelper.delayMillisecond(150);

        // And when: getting the same ocurredOn date after some delay
        Long ocurredOnNext = collectionCreatedEvent.occurredOn();

        // Then: the ocurredOn date stays the same
        assertThat(occurredOnFirst).isEqualTo(ocurredOnNext);
    }

    @Test
    public void eventIdIsConsistent() {
        CollectionCreatedEvent collectionCreatedEvent = TestHelper.aCollectionCreatedEvent();

        String firstEventId = collectionCreatedEvent.eventId();
        String secondEventId = collectionCreatedEvent.eventId();

        assertThat(firstEventId).isEqualTo(secondEventId);
    }


}
