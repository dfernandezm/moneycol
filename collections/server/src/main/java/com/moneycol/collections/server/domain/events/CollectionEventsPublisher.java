package com.moneycol.collections.server.domain.events;

import com.moneycol.collections.server.domain.events.core.LocalEventPublisher;

/**
 *
 * Publish collection related events to a local bus
 *
 */
public class CollectionEventsPublisher extends LocalEventPublisher {
    public CollectionEventsPublisher() {
        super("collectionEvents");
    }
}
