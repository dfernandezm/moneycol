package com.moneycol.collections.server.domain.events.core;

import com.moneycol.collections.server.domain.events.CollectionNameModifiedEvent;

public class CollectionEventSubscriber extends LocalEventSubscriber {
    @Override
    public void handleEvent(DomainEvent domainEvent) {
        if (!(domainEvent instanceof CollectionNameModifiedEvent)) {
            //super.handleDeadEvent(domainEvent.ge);
            System.out.println("Not handled");
        } else {
            System.out.println("Handled");
        }
    }
}
