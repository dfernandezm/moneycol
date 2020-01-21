package com.moneycol.collections.server.infrastructure.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.Gson;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.util.CollectionUtils;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/firestore/src/main/java/com/example/firestore/Quickstart.java
//https://github.com/golobitch/fun7/blob/e0e641b449d4b6d46a2a13e3c9fd1f30c3722d4b/src/test/java/ch/golobit/fun7/service/MultiplayerServiceTest.java
// https://cloud.google.com/firestore/docs/quickstart-servers
//TODO: wrap in RX
//TODO: convert futures to Flowables
//TODO: https://github.com/google/guava/wiki/EventBusExplained
@Slf4j
@Primary
@Singleton
@Named("firebase")
public class FirebaseCollectionRepository implements CollectionRepository {

    private Firestore firestore;
    private Gson gson = new Gson();

    @Inject
    public FirebaseCollectionRepository(FirebaseProvider firebaseProvider)  {
        this.firestore = firebaseProvider.getFirestoreInstance();
    }

    @Override
    public Collection create(Collection collection) {

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", collection.name());
        data.put("description", collection.description());
        data.put("collectorId", collection.collector().id());

        try {
            DocumentReference documentReference = firestore.collection("collections").document(collection.id());
            ApiFuture<WriteResult> result = documentReference.set(data);
            log.info("Created collection with id {} at {}", documentReference.getId(), result.get().getUpdateTime());
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
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", collection.name());
        data.put("description", collection.description());
        data.put("collectorId", collection.collector().id());

        try {
            DocumentReference documentReference = firestore.collection("collections").document(collection.id());
            if (!documentReference.get().get().exists()) {
                throw new RuntimeException("Collection does not exist with id: " + collection.id());
            }

            documentReference.update(data).get();
            log.info("Updated collection with id {} to {}", collection.id(), gson.toJson(data));
            return collection;
        } catch (Exception e) {
            log.error("Error updating", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(CollectionId collectionId) {
        DocumentReference docRef = firestore.collection("collections").document(collectionId.id());
        try {
            docRef.delete().get();
            log.info("Collection deleted {}", collectionId.id());
        } catch (Exception e) {
            log.error("Error deleting", e);
        }
    }

    @Override
    public Collection byId(CollectionId collectionId) {
        try {
            DocumentReference docRef = firestore.collection("collections").document(collectionId.id());
            DocumentSnapshot documentSnapshot = docRef.get().get();
            String collectionName = documentSnapshot.getString("name");
            String collectionDescription = documentSnapshot.getString("description");
            String collectorId = documentSnapshot.getString("collectorId");
            Collector collector = Collector.withCollectorId(collectorId);
            Collection result = Collection.withNameAndDescription(collectionId,
                                                                collectionName,
                                                                collectionDescription,
                                                                collector);
            return result;
        } catch(Exception e) {
            log.error("Error querying data");
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Collection> byCollector(CollectorId collectorId) {
        return null;
    }

    private void printAllElementsOf(String firebaseCollectionName) {
        List<DocumentReference> docRefs = CollectionUtils.iterableToList(firestore.collection(firebaseCollectionName).listDocuments());
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
