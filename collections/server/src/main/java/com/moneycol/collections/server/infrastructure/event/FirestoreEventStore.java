package com.moneycol.collections.server.infrastructure.event;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.moneycol.collections.server.domain.events.EventStore;
import com.moneycol.collections.server.domain.events.core.DomainEvent;
import com.moneycol.collections.server.infrastructure.repository.CollectionNotFoundException;
import com.moneycol.collections.server.infrastructure.repository.FirestoreProvider;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class FirestoreEventStore implements EventStore {

    private Firestore firestore;

    @Inject
    public FirestoreEventStore(FirestoreProvider firestoreProvider)  {
        this.firestore = firestoreProvider.getFirestoreInstance();
    }

    @Override
    public void store(DomainEvent domainEvent) {
        try {

            DocumentReference docRef = firestore.collection("events").document(domainEvent.eventId());
            DocumentSnapshot documentSnapshot = docRef.get().get();
            if (documentSnapshot.exists()) {
                log.warn("Event with id {} already exists - skipping", domainEvent.eventId());
            } else {
                createEvent(domainEvent);
            }
        } catch(Exception e) {
            log.error("Error querying data existence for events", e);
        }
    }

    private void createEvent(DomainEvent domainEvent) {
        try {
            DocumentReference documentReference = firestore.collection("events").document(domainEvent.eventId());
            documentReference.set(domainEvent);

            log.info("Created event in Firestore with id {}",
                    documentReference.getId());
        } catch (Exception e) {
            log.error("Error creating event in Firestore", e);
        }
    }
}
