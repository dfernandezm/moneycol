package com.moneycol.indexer.infra;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Factory
public class FirestoreFactory {

    private final String DEFAULT_PROJECT_ID = "moneycol";
    private final String projectId;

    public FirestoreFactory(@Value("PROJECT_ID") String projectId) {
        //TODO: the value is not correctly picked up
        this.projectId = DEFAULT_PROJECT_ID;
    }

    @Bean
    public Firestore firestore() {
        GoogleCredentials credentials;
        try {
            credentials = GoogleCredentials.getApplicationDefault();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();
            FirebaseApp.initializeApp(options);
            return FirestoreClient.getFirestore();
        } catch (Throwable e) {
            log.error("Error initializing Firestore", e);
            throw new RuntimeException("Error initializing Firestore", e);
        }
    }

    @Bean
    public TaskListRepository taskListRepository() {
        return new FirestoreTaskListRepository(firestore());
    }
}
