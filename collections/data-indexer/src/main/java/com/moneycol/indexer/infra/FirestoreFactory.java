package com.moneycol.indexer.infra;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

;

@Slf4j
@Factory
@RequiredArgsConstructor
public class FirestoreFactory {

    private final FanOutConfigurationProperties fanOutConfigurationProperties;

    //TODO: com.google.common.base.Preconditions.checkState(Preconditions.java:502) at com.google.firebase.FirebaseApp.initializeApp(FirebaseApp.java:222) at com.google.firebase.FirebaseApp.initializeApp(FirebaseApp.java:215) at com.google.firebase.FirebaseApp.initializeApp(FirebaseApp.java:202) at com.moneycol.indexer.infra.FirestoreFactory.firestore(FirestoreFactory.java:33) at com.moneycol.indexer.infra.$FirestoreFactory$Firestore0$Definition.build(Unknown Source) at
    @Bean
    @Singleton
    public Firestore firestore() {
        GoogleCredentials credentials;
        try {
            credentials = GoogleCredentials.getApplicationDefault();
            FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
                    .setCredentials(credentials)
                    .setProjectId(fanOutConfigurationProperties.getGcpProjectId())
                    .build();
            log.info("Initializing Firestore with project ID {}",
                    fanOutConfigurationProperties.getGcpProjectId());
            return firestoreOptions.getService();
        } catch (Throwable e) {
            log.error("Error initializing Firestore", e);
            throw new RuntimeException("Error initializing Firestore", e);
        }
    }

    @Bean
    @Singleton
    public TaskListRepository taskListRepository(Firestore firestore) {
        return new FirestoreTaskListRepository(firestore);
    }
}
