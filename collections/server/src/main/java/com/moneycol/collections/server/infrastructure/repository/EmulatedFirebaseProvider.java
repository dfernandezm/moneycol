package com.moneycol.collections.server.infrastructure.repository;

import com.google.cloud.NoCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;

@Singleton
@Requires(env="test")
public class EmulatedFirebaseProvider implements FirebaseProvider {
    private static final String LOCAL_FIRESTORE_EMULATOR_HOST = "127.0.0.1:8080";
    private static final String TEST_PROJECT_ID = "testproject";

    @Override
    public Firestore getFirestoreInstance() {
        FirestoreOptions foptions = FirestoreOptions.getDefaultInstance().toBuilder()
                .setHost(LOCAL_FIRESTORE_EMULATOR_HOST)
                .setCredentials(NoCredentials.getInstance())
                .setProjectId(TEST_PROJECT_ID)
                .build();

        return foptions.getService();
    }
}
