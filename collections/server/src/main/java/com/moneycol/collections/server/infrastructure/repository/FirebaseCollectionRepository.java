package com.moneycol.collections.server.infrastructure.repository;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.CollectorId;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.util.CollectionUtils;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/firestore/src/main/java/com/example/firestore/Quickstart.java
//https://github.com/golobitch/fun7/blob/e0e641b449d4b6d46a2a13e3c9fd1f30c3722d4b/src/test/java/ch/golobit/fun7/service/MultiplayerServiceTest.java
//TODO: convert futures to Flowables
@Slf4j
@Primary
@Singleton
@Named("firebase")
public class FirebaseCollectionRepository implements CollectionRepository {

    private Firestore firestore;

    @Inject
    public FirebaseCollectionRepository(SourceCredentials sourceCredentials)  {

        log.info("Reading credentials key for Firestore: {}", sourceCredentials.getCredentials());
        //TODO: WARNING: Your application has authenticated using end user credentials from Google Cloud SDK. We recommend that most server applications use service accounts instead. If your application continues to use end user credentials from Cloud SDK, you might receive a "quota exceeded" or "API not enabled" error. For more information about service accounts, see https://cloud.google.com/docs/authentication/.
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
        //printAllElementsOf("users");
        //createFirebaseCollectionIfNotExists("collections");
//        Collection col =  Collection.withNameAndDescription(CollectionId.of(Id.randomId()), "aName",
//                "adesc", Collector.of(CollectorId.of("idCol")));
//
        //TODO: wrap in RX
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", collection.name());
        data.put("description", collection.description());
        data.put("collectorId", collection.collector().id());

        try {
            DocumentReference documentReference = firestore.collection("collections").add(data).get();

            ApiFuture<WriteResult> result = documentReference.set(data);
            log.info("Reference is {}", result.get());
        } catch (Exception e) {
            log.error("Error creating", e);
        }

        return collection;
    }

    public Single<Boolean> firebaseCollectionExistsSingle(String collectionName) {
        Single<QuerySnapshot> s = Single.fromFuture(firestore.collection(collectionName).get());
        return s.map(query -> query.size() > 0);
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
