package com.moneycol.collections.server;

import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.events.CollectionCreatedEvent;

import java.util.concurrent.TimeUnit;

public class TestHelper {

    public static void delaySecond(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void delayMillisecond(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static CollectionCreatedEvent aCollectionCreatedEvent() {
        String collectionName = "collectionName";
        String collectionDescription = "Collection Description";
        return CollectionCreatedEvent.builder()
                .collectionId(CollectionId.randomId())
                .name(collectionName)
                .description(collectionDescription)
                .build();
    }
}
