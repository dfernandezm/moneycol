package com.moneycol.collections.server;

import com.moneycol.collections.server.application.CollectionApplicationService;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CollectionDTO;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import com.moneycol.collections.server.domain.base.Id;
import com.moneycol.collections.server.infrastructure.repository.EmulatedFirebaseProvider;
import com.moneycol.collections.server.infrastructure.repository.FirebaseCollectionRepository;
import com.moneycol.collections.server.infrastructure.repository.FirebaseProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Use emulator support for Firebase:
 * Frontend folder has firebase.json, that needs to be around
 * firebase emulators:start
 *
 */
public class CollectionApplicationServiceTest {

    @ParameterizedTest
    @CsvSource({"Banknotes, \"A collection for storing my banknotes in London\"",
                "\"Bankotes of the world\", \"A collection for storing my banknotes in the world\""})
    public void shouldCreateBasicCollection(String name, String description) {

       CollectionRepository collectionRepo = mockRepository();

        // Given
        String collectorId = UUID.randomUUID().toString();
        CollectionDTO createCollectionDTO = new CollectionDTO(name, description, collectorId);
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepo);

        // when
        CollectionCreatedResult collectionCreatedResult = cas.createCollection(createCollectionDTO);

        // then
        verify(collectionRepo, times(1)).create(any());
        assertEquals(collectionCreatedResult.getName(), name);
        assertEquals(collectionCreatedResult.getDescription(), description);
        assertEquals(collectionCreatedResult.getCollectorId(), collectorId);
        assertNotNull(collectionCreatedResult.getCollectionId());
    }


    @Test
    public void firebaseCollectionShouldntExist() {
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        collectionRepository.firebaseCollectionExistsSingle("collections").subscribe(e -> {
            System.out.print("Result: " + e);

        }, error -> {
            System.out.print("Error: " + error);
        });
    }

    //TODO: this test actually creates collections in firestore, so needs to mock the Firestore
    @ParameterizedTest
    @CsvSource({"Banknotes, \"A collection for storing my banknotes in London\"",
            "\"Bankotes of the world\", \"A collection for storing my banknotes in the world\""})
    public void firebaseCollectionCreationForReal(String name, String description) {

        FirebaseProvider f = new EmulatedFirebaseProvider();

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        String collectorId = UUID.randomUUID().toString();

        Collection col =  Collection.withNameAndDescription(CollectionId.of(Id.randomId()), name,
                description, Collector.of(CollectorId.of(collectorId)));

        collectionRepository.create(col);
        String id = col.id();

        Collection res = collectionRepository.byId(CollectionId.of(id));

        assertEquals(id, res.id());

        collectionRepository.delete(CollectionId.of(id));
    }

    @ParameterizedTest
    @CsvSource({"Banknotes, \"A collection for storing my banknotes in London\",BanknotesUpdated",
            "\"Bankotes of the world\", \"A collection for storing my banknotes in the world\",\"Banknotes only\""})
    public void updateCollectionTest(String name, String description, String newName) {

        FirebaseProvider f = new EmulatedFirebaseProvider();

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        String collectorId = UUID.randomUUID().toString();

        Collection col =  Collection.withNameAndDescription(CollectionId.of(Id.randomId()), name,
                description, Collector.of(CollectorId.of(collectorId)));

        collectionRepository.create(col);

        Collection toUpdate =  Collection.withNameAndDescription(CollectionId.of(col.id()), newName,
                description, Collector.of(CollectorId.of(collectorId)));

        Collection updated = collectionRepository.update(toUpdate);

        assertEquals(updated.name(), newName);
        collectionRepository.delete(CollectionId.of(updated.id()));
    }

    private CollectionRepository mockRepository() {
        CollectionRepository collectionRepo = Mockito.mock(CollectionRepository.class);
        Mockito.when(collectionRepo.create(any())).thenAnswer((r) -> r.getArgument(0));
        return collectionRepo;
    }

}
