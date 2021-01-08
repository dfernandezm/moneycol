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
import com.moneycol.collections.server.infrastructure.util.LambdaErrorHandlers;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.util.CollectionUtils;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.VisibleForTesting;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Implementation of Collection repository using firestore
 *
 *
 * Firestore subcollections usecases:
 * https://firebase.google.com/docs/firestore/manage-data/structure-data?authuser=0
 * https://firebase.google.com/docs/firestore/manage-data/transactions#batched-writes
 *
 *
 * Some samples
 *
 * see https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/firestore/src/main/java/com/example/firestore/Quickstart.java
 * see https://github.com/golobitch/fun7/blob/e0e641b449d4b6d46a2a13e3c9fd1f30c3722d4b/src/test/java/ch/golobit/fun7/service/MultiplayerServiceTest.java
 * see https://cloud.google.com/firestore/docs/quickstart-servers
 *
 */
@Slf4j
@Primary
@Singleton
@Named("firebase")
public class FirebaseCollectionRepository implements CollectionRepository {

    private Firestore firestore;
    private Gson gson = new Gson();

    @Inject
    public FirebaseCollectionRepository(FirestoreProvider firestoreProvider)  {
        this.firestore = firestoreProvider.getFirestoreInstance();
    }

    @Override
    public Collection create(Collection collection) {
        return createWithSupplier(collection);
    }

    @VisibleForTesting
    public Collection createWithSupplier(Collection collection) {
        return LambdaErrorHandlers.handleCheckedSupplier(() -> doCreateCollection(collection)).get();
    }

    private Collection doCreateCollection(Collection collection) throws InterruptedException, ExecutionException {
        Map<String, Object> data = toMapData(collection);
        DocumentReference docRef = firestore.collection("collections").document(collection.id());
        ApiFuture<WriteResult> result = docRef.set(data);
        log.info("Created collection with collectionId {} for collector {} at {}",
                docRef.getId(),
                collection.collector().id(),
                result.get().getUpdateTime());
        return collection;
    }

    private Map<String, Object> toMapData(Collection collection) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", collection.name());
        data.put("description", collection.description());
        data.put("collectorId", collection.collector().id());
        return data;
    }

    public Single<Boolean> firebaseCollectionExistsSingle(String collectionName) {
        Single<QuerySnapshot> s = Single.fromFuture(firestore.collection(collectionName).get());
        return s.map(query -> query.size() > 0);
    }

    @Override
    public Collection update(Collection collectionToUpdate) {
        Map<String, Object> collectionDataToUpdate = new LinkedHashMap<>();
        collectionDataToUpdate.put("name", collectionToUpdate.name());
        collectionDataToUpdate.put("description", collectionToUpdate.description());

        try {
            DocumentReference documentReference =
                    firestore.collection("collections").document(collectionToUpdate.id());
            validateExistence(documentReference, collectionToUpdate.id());

            documentReference.update(collectionDataToUpdate).get();
            CollectionReference collectionReference = firestore.collection("collections")
                     .document(collectionToUpdate.id())
                     .collection("items");

            updateBatchOfCollectionItems(collectionReference, collectionToUpdate.items());

            log.info("Updated collection with collectionId {} to {}", collectionToUpdate.id(), gson.toJson(collectionDataToUpdate));
            return collectionToUpdate;
        }  catch (CollectionNotFoundException cnfe) {
            throw cnfe;
        }catch (Exception e) {
            log.error("Error updating", e);
            throw new RuntimeException(e);
        }
    }

    private void updateBatchOfCollectionItems(CollectionReference currentItems, List<CollectionItem> toUpdateItems) {

        // deletions
        WriteBatch batch = firestore.batch();
        currentItems.listDocuments().forEach(doc -> {
            if (!toUpdateItems.contains(CollectionItem.of(doc.getId()))) {
                batch.delete(doc);
            }
        });

        // additions
        toUpdateItems.forEach(item -> {
            DocumentReference itemRef = currentItems.document(item.getItemId());
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
            validateExistence(docRef, collectionId.id());
            deleteItems(docRef);
            docRef.delete().get();
            log.info("Collection deleted {}", collectionId.id());
        }  catch (CollectionNotFoundException cnfe) {
            throw cnfe;
        } catch (Exception e) {
            log.error("Error deleting", e);
        }
    }

    private void deleteItems(DocumentReference docRef) throws InterruptedException, ExecutionException {
        CollectionReference itemsReference = docRef.collection("items");
        int itemsSize = itemsReference.get().get().size();
        if (itemsReference.get().get().size() > 0) {
            log.info("[ {} ] items are present in Collection to delete", itemsSize);
            deleteSubCollectionInBatches(itemsReference, 20);
        }
    }

    private void validateExistence(DocumentReference docRef, String id) throws InterruptedException, ExecutionException {
        if (!docRef.get().get().exists()) {
            throw new CollectionNotFoundException("Collection does not exist with collectionId: " + id);
        }
    }

    private void deleteSubCollectionInBatches(CollectionReference collection, int batchSize) {
        log.info("Deleting batch of documents for subcollection with collectionId {} (batch: {})",
                collection.getId(),
                batchSize);
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
            int deleted = 0;

            // future.get() blocks on document retrieval
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                log.info("Deleting document with collectionId in batch: {}", document.getId());
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
            DocumentReference collectionDocRef = firestore.collection("collections").document(collectionId.id());
            DocumentSnapshot collectionDocSnapshot = collectionDocRef.get().get();
            if (collectionDocSnapshot.exists()) {
                return findExistingCollection(collectionId, collectionDocRef, collectionDocSnapshot);
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

    private Collection findExistingCollection(CollectionId collectionId, DocumentReference collectionDocRef, DocumentSnapshot collectionDocSnapshot) {
        List<CollectionItem> items = extractItemsForCollection(collectionDocRef);
        Collector collector = extractCollector(collectionDocSnapshot);
        Collection collection = extractBaseCollection(collectionId, collectionDocSnapshot, collector);
        collection.addItems(items);
        return collection;
    }

    private Collection extractBaseCollection(CollectionId collectionId, DocumentSnapshot collectionDocSnapshot, Collector collector) {
        String collectionName = collectionDocSnapshot.getString("name");
        String collectionDescription = collectionDocSnapshot.getString("description");
        return Collection.withNameAndDescription(collectionId,
                collectionName,
                collectionDescription,
                collector);
    }

    private Collector extractCollector(DocumentSnapshot collectionDocSnapshot) {
        String collectorId = collectionDocSnapshot.getString("collectorId");
        return Collector.withCollectorId(collectorId);
    }

    private List<CollectionItem> extractItemsForCollection(DocumentReference docRef) {
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

            // Any other collection (different collectionId) has the same name
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
                Collector.withStringCollectorId(collectorId));
    }
}
