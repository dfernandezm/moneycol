package com.moneycol.collections.server;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionItem;
import com.moneycol.collections.server.infrastructure.repository.CollectionNotFoundException;
import com.moneycol.collections.server.infrastructure.repository.EmulatedFirebaseProvider;
import io.micronaut.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
public class FirebaseUtil {
    private static Firestore firestore;

    public static void init() {
        firestore = new EmulatedFirebaseProvider().getFirestoreInstance();
    }

    public static void createCollection(String id, String name, String description, String collectorId) {
        createCollectionInternal(id, name, description, collectorId, new ArrayList<>());
    }

    public static void createCollectionWithItems(String id, String name, String description,
                                                 String collectorId, List<CollectionItem> collectionItems) {
        createCollectionInternal(id, name, description, collectorId, collectionItems);
    }

    private static void createCollectionInternal(String id, String name, String description, String collectorId,
                                        List<CollectionItem> collectionItems) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("description", description);
        data.put("collectorId", collectorId);

        try {
            DocumentReference documentReference = firestore.collection("collections").document(id);
            documentReference.set(data);
            createItems(collectionItems, documentReference);
        } catch (Exception e) {
            throw new RuntimeException("Error creating collection", e);
        }
    }

    private static void createItems(List<CollectionItem> collectionItems, DocumentReference documentReference) {
        CollectionReference colRef = documentReference.collection("items");
        collectionItems.forEach(item -> {
            try {
                colRef.document(item.getItemId()).set(item).get();
            } catch (Exception e) {
                throw new RuntimeException("Error inserting item: " + item.getItemId(), e);
            }
        });
    }

    //NOTE: the Firestore emulator does not implement this
    //https://stackoverflow.com/questions/54287565/firestore-query-for-subcollections-on-a-deleted-document
    public static List<String> findSubCollectionsOfMissingDocuments(String collectionId) {
        List<String> subcollectionIds = new ArrayList<>();
        List<DocumentReference> docRefs =
                CollectionUtils.iterableToList(firestore.collection("collections").listDocuments());
        ApiFuture<List<DocumentSnapshot>> result = firestore.getAll(docRefs.toArray(new DocumentReference[0]));

        try {
            result.get().forEach(docSnapshot -> {
                if (!docSnapshot.exists()) {
                    docSnapshot.getReference().listCollections().forEach(colRef -> subcollectionIds.add(colRef.getId()));
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return subcollectionIds;

    }

    public static Collection findCollectionById(String collectionId) {
        DocumentReference ref = firestore.collection("collections").document(collectionId);
        try {
            DocumentSnapshot documentSnapshot = ref.get().get();

            if (!documentSnapshot.exists()) {
               throw new CollectionNotFoundException("Collection with Id " + collectionId + " not found");
            }

            return documentSnapshot.toObject(Collection.class);
        } catch (CollectionNotFoundException cnfe) {
            throw cnfe;
        } catch (Exception e) {
            throw new RuntimeException("Error finding by collectionId", e);
        }
    }

    public static void deleteAllCollections() {
        deleteCollection(firestore.collection("collections"),10);
    }

    public static List<String> findItemsForCollection(String collectionId) {
        List<String> itemIds = new ArrayList<>();
        firestore
                .collection("collections")
                .document(collectionId)
                .collection("items")
                .listDocuments()
                .forEach(documentReference -> {
                    try {
                        itemIds.add(documentReference.get().get().getString("itemId"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                });
        return itemIds;
    }

    private static void deleteCollection(CollectionReference collection, int batchSize) {
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
            int deleted = 0;
            // future.get() blocks on document retrieval
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete();
                ++deleted;
            }
            if (deleted >= batchSize) {
                // retrieve and delete another batch
                deleteCollection(collection, batchSize);
            }
        } catch (Exception e) {
            System.err.println("Error deleting collection : " + e.getMessage());
        }
    }

}
