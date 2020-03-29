package com.moneycol.collections.server.infrastructure.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;
import com.google.gson.Gson;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionItem;
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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

//https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/firestore/src/main/java/com/example/firestore/Quickstart.java
//https://github.com/golobitch/fun7/blob/e0e641b449d4b6d46a2a13e3c9fd1f30c3722d4b/src/test/java/ch/golobit/fun7/service/MultiplayerServiceTest.java
// https://cloud.google.com/firestore/docs/quickstart-servers
//TODO: wrap in RX
//TODO: convert futures to Flowables
//TODO: https://github.com/google/guava/wiki/EventBusExplained
// Querying: https://firebase.google.com/docs/firestore/query-data/get-data
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
            log.info("Created collection with id {} for collector {} at {}",
                    documentReference.getId(),
                    collection.collector().id(),
                    result.get().getUpdateTime());
        } catch (Exception e) {
            log.error("Error creating", e);
            throw new RuntimeException("Error creating collection", e);
        }

        return collection;
    }

    public Single<Boolean> firebaseCollectionExistsSingle(String collectionName) {
        Single<QuerySnapshot> s = Single.fromFuture(firestore.collection(collectionName).get());
        return s.map(query -> query.size() > 0);
    }

    // Firestore subcollections usecases:
    // https://firebase.google.com/docs/firestore/manage-data/structure-data?authuser=0
    // https://firebase.google.com/docs/firestore/manage-data/transactions#batched-writes
    @Override
    public Collection update(Collection collection) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", collection.name());
        data.put("description", collection.description());

        try {
            DocumentReference documentReference =
                    firestore.collection("collections").document(collection.id());
            if (!documentReference.get().get().exists()) {
                throw new CollectionNotFoundException("Collection does not exist with id: " + collection.id());
            }

            documentReference.update(data).get();
            CollectionReference collectionReference = firestore.collection("collections")
                     .document(collection.id())
                     .collection("items");

            updateBatchOfCollectionItems(collectionReference, collection.items());

            log.info("Updated collection with id {} to {}", collection.id(), gson.toJson(data));
            return collection;
        }  catch (CollectionNotFoundException cnfe) {
            throw cnfe;
        }catch (Exception e) {
            log.error("Error updating", e);
            throw new RuntimeException(e);
        }
    }

    private void updateBatchOfCollectionItems(CollectionReference items, List<CollectionItem> collectionItems) {
        // deletions
        WriteBatch batch = firestore.batch();
        items.listDocuments().forEach(doc -> {
            if (!collectionItems.contains(CollectionItem.of(doc.getId()))) {
                batch.delete(doc);
            }
        });

        // additions
        collectionItems.forEach(item -> {
            DocumentReference itemRef = items.document(item.getItemId());
            batch.set(itemRef, item);
        });

        ApiFuture<List<WriteResult>> results = batch.commit();

        try {
            results.get().forEach(writeResult -> log.info("Updated item at {}", writeResult.getUpdateTime()));

        } catch (InterruptedException | ExecutionException ie) {
            log.error("Error inserting items batch", ie);
            throw new RuntimeException("Error inserting items batch", ie);
        }
    }

    @Override
    public void delete(CollectionId collectionId) {
        DocumentReference docRef = firestore.collection("collections").document(collectionId.id());
        try {
            if (!docRef.get().get().exists()) {
                throw new CollectionNotFoundException("Collection does not exist with id: " + collectionId.id());
            }

            CollectionReference itemsReference = docRef.collection("items");
            int itemsSize = itemsReference.get().get().size();
            if (itemsReference.get().get().size() > 0) {
                log.info("[ {} ] items are present in Collection to delete", itemsSize);
                deleteSubCollectionInBatches(itemsReference, 20);
            }

            docRef.delete().get();
            log.info("Collection deleted {}", collectionId.id());
        }  catch (CollectionNotFoundException cnfe) {
            throw cnfe;
        } catch (Exception e) {
            log.error("Error deleting", e);
        }
    }

    private void deleteSubCollectionInBatches(CollectionReference collection, int batchSize) {
        log.info("Deleting batch of documents for subcollection with id {} (batch: {})",
                collection.getId(),
                batchSize);
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
            int deleted = 0;
            // future.get() blocks on document retrieval
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                log.info("Deleting document with id in batch: {}", document.getId());
                document.getReference().delete();
                ++deleted;
            }
            if (deleted >= batchSize) {
                // retrieve and delete another batch
                deleteSubCollectionInBatches(collection, batchSize);
            }

            log.info("Finished deleting batch of documents fo collection: {}", collection.getId());

        } catch (Exception e) {
            log.error("Error deleting collection : " + e.getMessage());
        }
    }

    @Override
    public Collection byId(CollectionId collectionId) {
        try {

            DocumentReference docRef = firestore.collection("collections").document(collectionId.id());
            DocumentSnapshot documentSnapshot = docRef.get().get();
            if (documentSnapshot.exists()) {
                String collectionName = documentSnapshot.getString("name");
                String collectionDescription = documentSnapshot.getString("description");
                String collectorId = documentSnapshot.getString("collectorId");
                Collector collector = Collector.withCollectorId(collectorId);

                List<CollectionItem> items = findItemsForCollection(docRef);

                Collection collection = Collection.withNameAndDescription(collectionId,
                        collectionName,
                        collectionDescription,
                        collector);
                collection.addItems(items);

                return collection;
            } else {
                throw new CollectionNotFoundException("Collection with ID " + collectionId.id() + " not found");
            }
        } catch (CollectionNotFoundException cnfe) {
            throw cnfe;
        } catch(Exception e) {
            log.error("Error querying data", e);
            throw new RuntimeException(e);
        }
    }

    private List<CollectionItem> findItemsForCollection(DocumentReference docRef) {

            return  CollectionUtils
                    .iterableToList(docRef.collection("items")
                    .listDocuments())
                    .stream()
                    .map(this::fetchDocReferenceHandlingError)
                    .map(documentSnapshot -> documentSnapshot.toObject(CollectionItem.class))
                    .collect(Collectors.toList());
    }

    private DocumentSnapshot fetchDocReferenceHandlingError(DocumentReference docRef) {
        try {
            return docRef.get().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error returning items for collection");
            throw new RuntimeException("Error returning items for collection", e);
        }
    }

    @Override
    public List<Collection> byCollector(CollectorId collectorId) {
        try {

            Query query = firestore.collection("collections").whereEqualTo("collectorId", collectorId.id());
            ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();
            List<QueryDocumentSnapshot> queryDocumentSnapshots = querySnapshotFuture.get().getDocuments();

            return queryDocumentSnapshots
                            .stream()
                            .map(this::toCollection)
                            .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error querying collections for collectorId: {}", collectorId, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean existsWithName(String existingId, String name) {
        try {

            Query query = firestore
                    .collection("collections")
                    .whereEqualTo("name", name);
            ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();
            List<QueryDocumentSnapshot> queryDocumentSnapshots = querySnapshotFuture.get().getDocuments();

            if (existingId == null) {
                return queryDocumentSnapshots.size() > 0;
            }

            // Any other collection (different id) has the same name
            return queryDocumentSnapshots.stream().anyMatch(qde ->
                                                            !qde.getId().equals(existingId) &&
                                                            qde.getString("name").equals(name));
        } catch (Exception e) {
            log.error("Error querying collections for name: {}", name, e);
            throw new RuntimeException(e);
        }
    }

    private Collection toCollection(DocumentSnapshot documentSnapshot) {
        String collectionId = documentSnapshot.getId();
        String name = documentSnapshot.getString("name");
        String description = documentSnapshot.getString("description");
        String collectorId = documentSnapshot.getString("collectorId");
        return Collection.withNameAndDescription(
                CollectionId.of(collectionId), name, description,
                Collector.of(CollectorId.of(collectorId)));
    }
}
