package com.moneycol.indexer.infra;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Factory
public class FirestoreFactory {

    private final String PROJECT_ID = "moneycol";

    @Bean
    public Firestore firestore() {
        GoogleCredentials credentials;
        try {
            credentials = GoogleCredentials.getApplicationDefault();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(PROJECT_ID)
                    .build();
            FirebaseApp.initializeApp(options);
            return FirestoreClient.getFirestore();
        } catch (IOException e) {
            log.error("Error initializing Firestore", e);
            throw new RuntimeException("Error initializing Firestore", e);
        }
    }

    @Bean
    public TaskListRepository taskListRepository() {
        return new FirestoreTaskListRepository(firestore());
    }
}
