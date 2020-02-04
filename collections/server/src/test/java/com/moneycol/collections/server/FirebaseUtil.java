package com.moneycol.collections.server;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.moneycol.collections.server.infrastructure.repository.EmulatedFirebaseProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FirebaseUtil {
    private static Firestore firestore;

    public static void init() {
        firestore = new EmulatedFirebaseProvider().getFirestoreInstance();
    }

    public static void createCollection(String id, String name, String description, String collectorId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("description", description);
        data.put("collectorId", collectorId);

        try {
            DocumentReference documentReference = firestore.collection("collections").document(id);
            ApiFuture<WriteResult> result = documentReference.set(data);
            result.get();
        } catch (Exception e) {
            throw new RuntimeException("Error creating collection", e);
        }
    }

    public static void deleteAllCollections() {
        deleteCollection(firestore.collection("collections"),10);
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
