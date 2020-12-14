package com.moneycol.collections.server.infrastructure.repository;

import com.google.cloud.firestore.Firestore;

public interface FirestoreProvider {
    Firestore getFirestoreInstance();
}
