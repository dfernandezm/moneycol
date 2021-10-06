package com.moneycol.indexer.infra;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Factory
public class FirestoreFactory {

    //private final String DEFAULT_PROJECT_ID = "moneycol";
    @Inject
    private FanOutConfigurationProperties fanOutConfigurationProperties;

    @Inject
    private ApplicationContext applicationContext;
    //private final String projectId;

    // @Value("${gcp-project-id}") String projectId
//    public FirestoreFactory(FanOutConfigurationProperties fanOutConfigurationProperties) {
//        this.fanOutConfigurationProperties = fanOutConfigurationProperties;
//        //this.projectId = projectId == null ? DEFAULT_PROJECT_ID : projectId;
//    }

    @Bean
    @Singleton
    public Firestore firestore() {
        GoogleCredentials credentials;
        try {
            credentials = GoogleCredentials.getApplicationDefault();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(fanOutConfigurationProperties.getGcpProjectId())
                    .build();
            FirebaseApp.initializeApp(options);
            return FirestoreClient.getFirestore();
        } catch (Throwable e) {
            log.error("Error initializing Firestore", e);
            throw new RuntimeException("Error initializing Firestore", e);
        }
    }

    @Bean
    @Singleton
    public TaskListRepository taskListRepository() {
        return new FirestoreTaskListRepository(firestore());
    }
}
