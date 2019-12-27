package com.moneycol.collections.server.infrastructure.repository;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import com.moneycol.collections.server.domain.base.Id;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;

//https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/firestore/src/main/java/com/example/firestore/Quickstart.java

@Slf4j
@Primary
@Singleton
@Named("firebase")
public class FirebaseCollectionRepository implements CollectionRepository {

    private static String GOOGLE_CREDENTIALS_KEY_ENV_VAR_NAME = "GOOGLE_APPLICATION_CREDENTIALS";
    private static String PROJECT_ID = "moneycol";

    private Firestore firestore;

    @Inject
    public FirebaseCollectionRepository()  {

        log.info("Reading credentials key for Firestore: {}", System.getenv(GOOGLE_CREDENTIALS_KEY_ENV_VAR_NAME));

        try {
            FirestoreOptions opt  = FirestoreOptions.newBuilder()
                                            .setCredentials(GoogleCredentials.getApplicationDefault())
                                            .setProjectId("moneycol")
                                            .build();
            this.firestore = opt.getService();
            //TODO: connect asynchronously to avoid slow first call
        } catch (IOException e) {
            log.error("Error connecting to Firestore",e);
        }
    }

    @Override
    public Collection create(Collection collection) {
        printAllElementsOf("users");
        return Collection.withNameAndDescription(CollectionId.of(Id.randomId()), "aName",
                "adesc", Collector.of(CollectorId.of("idCol")));
    }

    @Override
    public Collection update(Collection collection) {
        return null;
    }

    @Override
    public void delete(CollectionId collectionId) {

    }

    @Override
    public Collection byId(CollectionId collectionId) {
        return null;
    }

    @Override
    public List<Collection> byCollector(CollectorId collectorId) {
        return null;
    }

    private void printAllElementsOf(String firebaseCollectionName) {
        List<DocumentReference> docRefs = CollectionUtils.iterableToList(firestore.collection("users").listDocuments());
        docRefs.forEach(docRef -> {
            try {
                Map<String, Object> d = docRef.get().get().getData();
                d.forEach((key, value) -> {
                    log.info("{} -> {}", key, value);
                });

            } catch(Exception e) {
                log.error("Error querying document", e);
            }
        });
    }
}
