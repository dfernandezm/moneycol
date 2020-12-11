package com.moneycol.collections.server;

import com.google.api.client.util.Lists;
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
import io.micronaut.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.FirestoreEmulatorContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class FirestoreHelper {

    public static final DockerImageName FIREBASE_EMULATOR_IMG =
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:316.0.0-emulators");
    private static Firestore firestore;

    public static EmulatedFirestoreProvider init(String emulatorEndpoint) {
        EmulatedFirestoreProvider firestoreProvider = new EmulatedFirestoreProvider();
        firestoreProvider.setEndpoint(emulatorEndpoint);
        firestore = firestoreProvider.getFirestoreInstance();
        return firestoreProvider;
    }

    public static EmulatedFirestoreProvider initContainer() {
        FirestoreEmulatorContainer firestoreEmulatorContainer = new FirestoreEmulatorContainer(FIREBASE_EMULATOR_IMG);
        firestoreEmulatorContainer.start();
        String endpoint = firestoreEmulatorContainer.getEmulatorEndpoint();
        log.info("Endpoint: " + endpoint);
        EmulatedFirestoreProvider emulatedFirestoreProvider = init(endpoint);
        firestore = emulatedFirestoreProvider.getFirestoreInstance();
        return emulatedFirestoreProvider;
    }

    public static void init() {
        firestore = new EmulatedFirestoreProvider().getFirestoreInstance();
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
            System.out.println("Created collection " + id);
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


    public static Map<String, Object> findEventData(String eventsPath, String eventId) {
        try {
            DocumentReference documentReference = firestore.collection(eventsPath).document(eventId);
            DocumentSnapshot documentSnapshot = documentReference.get().get();
            if (documentSnapshot.exists()) {
                return documentSnapshot.getData();
            } else {
                throw new RuntimeException("Event in " + eventsPath + " does not exist with id " + eventId);
            }
        } catch (InterruptedException | ExecutionException ie) {
            throw new RuntimeException("Event in " + eventsPath + " does not exist with id " + eventId, ie);
        }
    }

    public static List<Map<String, Object>> findAllEvents() {
        try {
            Iterable<DocumentReference> documentReferences = firestore.collection("events").listDocuments();
            return Lists.newArrayList(documentReferences).stream().map(documentReference -> {
                try {
                    return documentReference.get().get().getData();
                } catch (InterruptedException | ExecutionException ie) {
                    throw new RuntimeException("Error getting data", ie);
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting data", e);
        }
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
